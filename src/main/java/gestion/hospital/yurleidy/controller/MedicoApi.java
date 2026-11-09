package gestion.hospital.yurleidy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Tag(name = "Médicos", description = "Crear, editar y eliminar médicos. Solo ADMINISTRADOR. "
        + "También llena el campo de selección \"Médico\" del formulario de consultorio")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Unauthorized: no ha iniciado sesión", content = @Content),
    @ApiResponse(responseCode = "403", description = "Forbidden: solo el ADMINISTRADOR puede usar este servicio", content = @Content)
})
@RestController
@RequestMapping("/api/medicos")
public class MedicoApi {

    private final MedicoRepository repo;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaRepository consultaRepository;
    private final PasswordEncoder passwordEncoder;

    public MedicoApi(MedicoRepository repo, UsuarioRepository usuarioRepository,
                     ConsultaRepository consultaRepository, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // datos del médico y de su cuenta para iniciar sesión
    public record MedicoRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
            @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$", message = "El nombre solo admite letras y espacios") String nombre,
            @NotBlank(message = "El apellido es obligatorio")
            @Size(max = 50, message = "El apellido no puede superar 50 caracteres")
            @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$", message = "El apellido solo admite letras y espacios") String apellido,
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es válido")
            @Size(max = 100, message = "El correo no puede superar 100 caracteres") String correo,
            @Size(max = 50, message = "La contraseña no puede superar 50 caracteres") String contrasena) {}

    public record MedicoResponse(Integer idMedico, String nombre, String apellido, String correo) {}

    @Operation(summary = "Lista los médicos",
               description = "Solo ADMINISTRADOR. Devuelve los médicos de la tabla independiente medico "
                           + "(nombre, apellido y correo). Llena el desplegable \"Médico\" del formulario.")
    @ApiResponse(responseCode = "200", description = "OK: lista de médicos")
    @GetMapping
    public List<MedicoResponse> listar() {
        return repo.findAll().stream().map(this::respuesta).toList();
    }

    @Operation(summary = "Crea un médico",
               description = "Solo ADMINISTRADOR. Crea el médico y su usuario con rol MEDICO. "
                           + "Ejemplo del cuerpo: {\"nombre\": \"Sofía\", \"apellido\": \"Rincón\", "
                           + "\"correo\": \"medico3@clinica.com\", \"contrasena\": \"1234\"}")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK: médico creado"),
        @ApiResponse(responseCode = "400", description = "Bad Request: datos inválidos o correo ya registrado", content = @Content)
    })
    @Transactional
    @PostMapping
    public MedicoResponse crear(@Valid @RequestBody MedicoRequest datos) {
        if (datos.contrasena() == null || datos.contrasena().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        validarContrasena(datos.contrasena());
        if (usuarioRepository.existsByCorreo(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con el correo " + datos.correo());
        }
        Usuario u = new Usuario();
        u.setNombre(datos.nombre());
        u.setApellido(datos.apellido());
        u.setCorreo(datos.correo());
        u.setContrasena(passwordEncoder.encode(datos.contrasena()));
        u.setTipoUsuario("MEDICO");
        usuarioRepository.save(u);

        Medico m = new Medico();
        m.setNombre(datos.nombre() + " " + datos.apellido());
        m.setIdUsuario(u.getIdUsuario());
        return respuesta(repo.save(m));
    }

    @Operation(summary = "Edita un médico",
               description = "Solo ADMINISTRADOR. Se envían nombre, apellido y correo. "
                           + "Si la contraseña va vacía, no se cambia.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK: médico actualizado"),
        @ApiResponse(responseCode = "400", description = "Bad Request: datos inválidos o correo ya registrado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found: no existe el médico", content = @Content)
    })
    @Transactional
    @PutMapping("/{id}")
    public MedicoResponse editar(@PathVariable Integer id, @Valid @RequestBody MedicoRequest datos) {
        Medico m = buscar(id);
        Usuario u = usuarioRepository.findById(m.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario del médico " + id));

        if (!u.getCorreo().equalsIgnoreCase(datos.correo()) && usuarioRepository.existsByCorreo(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con el correo " + datos.correo());
        }
        u.setNombre(datos.nombre());
        u.setApellido(datos.apellido());
        u.setCorreo(datos.correo());
        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            validarContrasena(datos.contrasena());
            u.setContrasena(passwordEncoder.encode(datos.contrasena()));
        }
        usuarioRepository.save(u);

        m.setNombre(datos.nombre() + " " + datos.apellido());
        return respuesta(repo.save(m));
    }

    @Operation(summary = "Elimina un médico",
               description = "Solo ADMINISTRADOR. Elimina el médico y su usuario. "
                           + "No se permite si el médico tiene consultas asignadas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK: médico eliminado"),
        @ApiResponse(responseCode = "400", description = "Bad Request: el médico tiene consultas asignadas", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not Found: no existe el médico", content = @Content)
    })
    @Transactional
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        Medico m = buscar(id);
        if (consultaRepository.existsByMedicoIdMedico(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede eliminar a " + m.getNombre() + " porque tiene consultas asignadas");
        }
        repo.delete(m);
        usuarioRepository.deleteById(m.getIdUsuario());
    }

    private void validarContrasena(String contrasena) {
        if (contrasena.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener mínimo 4 caracteres");
        }
    }

    private Medico buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el médico " + id));
    }

    private MedicoResponse respuesta(Medico m) {
        Usuario u = usuarioRepository.findById(m.getIdUsuario()).orElse(new Usuario());
        return new MedicoResponse(m.getIdMedico(), u.getNombre(), u.getApellido(), u.getCorreo());
    }
}
