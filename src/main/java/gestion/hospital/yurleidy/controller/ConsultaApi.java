package gestion.hospital.yurleidy.controller;

import java.text.Normalizer;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import gestion.hospital.yurleidy.model.Consulta;
import gestion.hospital.yurleidy.model.Medico;
import gestion.hospital.yurleidy.model.Usuario;
import gestion.hospital.yurleidy.repository.ConsultaRepository;
import gestion.hospital.yurleidy.repository.MedicoRepository;
import gestion.hospital.yurleidy.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Consultas", description = "Consultas médicas y asignación de consultorios")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Unauthorized: no ha iniciado sesión", content = @Content),
    @ApiResponse(responseCode = "403", description = "Forbidden: su rol no tiene permiso para esta acción", content = @Content)
})
@RestController
@RequestMapping("/api/consultas")
public class ConsultaApi {

    private final ConsultaRepository repo;
    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;

    public ConsultaApi(ConsultaRepository repo, MedicoRepository medicoRepository,
                       UsuarioRepository usuarioRepository) {
        this.repo = repo;
        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // el paciente solo envía el horario
    public record HorarioRequest(LocalTime horaInicio, LocalTime horaFin) {}

    @Operation(summary = "Lista las consultas según el rol",
               description = "ADMINISTRADOR: todas las consultas. "
                           + "MEDICO: solo las consultas asignadas a él, sin ver las de otros médicos. "
                           + "PACIENTE: solo sus citas (las que tienen su nombre completo como nombre del paciente). "
                           + "Devuelve nombre del paciente, motivo, número de consultorio, horario y médico.")
    @ApiResponse(responseCode = "200", description = "OK: lista de consultas")
    @GetMapping
    public List<Consulta> listar(Authentication auth) {
        Usuario u = usuarioActual(auth);
        return switch (u.getTipoUsuario()) {
            case "ADMINISTRADOR" -> repo.findAll();
            case "MEDICO" -> medicoRepository.findByIdUsuario(u.getIdUsuario())
                    .map(m -> repo.findByMedicoIdMedico(m.getIdMedico()))
                    .orElse(List.of());
            default -> repo.findByNombrePacienteIgnoreCase(nombreCompleto(u));
        };
    }

    @Operation(summary = "Registra un consultorio (asigna una consulta)",
               description = "Solo ADMINISTRADOR. Se envía: nombrePaciente (alfanumérico, máximo 40 caracteres; "
                           + "debe ser el nombre completo de un paciente registrado), "
                           + "motivo (máximo 100 caracteres), numeroConsultorio (numérico), horaInicio y horaFin (HH:mm) "
                           + "y medico ({\"idMedico\": 1}). Ejemplo: {\"nombrePaciente\": \"Carlos Pérez\", "
                           + "\"motivo\": \"Control general\", \"numeroConsultorio\": 101, \"horaInicio\": \"08:00\", "
                           + "\"horaFin\": \"08:30\", \"medico\": {\"idMedico\": 1}}")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK: consulta registrada"),
        @ApiResponse(responseCode = "400", description = "Bad Request: datos inválidos o el paciente no está registrado", content = @Content)
    })
    @PostMapping
    public Consulta registrar(@Valid @RequestBody Consulta c) {
        validarHorario(c.getHoraInicio(), c.getHoraFin());
        Integer idMedico = c.getMedico().getIdMedico();
        Medico medico = (idMedico == null ? null : medicoRepository.findById(idMedico).orElse(null));
        if (medico == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El médico seleccionado no existe");
        }
        c.setIdConsulta(null);
        c.setNombrePaciente(nombreCompleto(buscarPaciente(c.getNombrePaciente())));
        c.setMedico(medico);
        return repo.save(c);
    }

    @Operation(summary = "Lista los pacientes registrados",
               description = "Solo ADMINISTRADOR. Devuelve el nombre completo de cada paciente; "
                           + "el nombre del paciente de una consulta debe ser uno de estos.")
    @ApiResponse(responseCode = "200", description = "OK: nombres de los pacientes")
    @GetMapping("/pacientes")
    public List<String> pacientes() {
        return usuarioRepository.findByTipoUsuario("PACIENTE").stream().map(this::nombreCompleto).toList();
    }

    @Operation(summary = "Actualiza el horario de una cita",
               description = "Solo PACIENTE y solo en las citas que tiene asignadas; únicamente modifica el horario. "
                           + "Si la cita es de otro paciente responde 403. "
                           + "Ejemplo del cuerpo: {\"horaInicio\": \"10:00\", \"horaFin\": \"10:30\"}")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK: horario actualizado"),
        @ApiResponse(responseCode = "400", description = "Bad Request: horario inválido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found: no existe la cita", content = @Content)
    })
    @PatchMapping("/{id}/horario")
    public Consulta cambiarHorario(@PathVariable Integer id, @RequestBody HorarioRequest body,
                                   Authentication auth) {
        validarHorario(body.horaInicio(), body.horaFin());
        Consulta c = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la cita " + id));

        if (!c.getNombrePaciente().equalsIgnoreCase(nombreCompleto(usuarioActual(auth)))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo puede actualizar el horario de las citas que tiene asignadas");
        }
        c.setHoraInicio(body.horaInicio());
        c.setHoraFin(body.horaFin());
        return repo.save(c);
    }

    private void validarHorario(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de inicio y la hora de finalización son obligatorias");
        }
        if (!fin.isAfter(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de finalización debe ser posterior a la hora de inicio");
        }
    }

    private Usuario usuarioActual(Authentication auth) {
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private String nombreCompleto(Usuario u) {
        return u.getNombre() + " " + u.getApellido();
    }

    // busca el paciente sin importar mayúsculas, tildes ni espacios de más
    private Usuario buscarPaciente(String nombre) {
        String buscado = normalizar(nombre);
        return usuarioRepository.findByTipoUsuario("PACIENTE").stream()
                .filter(p -> normalizar(nombreCompleto(p)).equals(buscado))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe un paciente registrado con el nombre " + nombre));
    }

    private String normalizar(String texto) {
        String sinEspacios = texto.trim().replaceAll("\\s+", " ");
        return Normalizer.normalize(sinEspacios, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }
}
