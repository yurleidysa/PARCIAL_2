package gestion.hospital.yurleidy.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import gestion.hospital.yurleidy.model.Medico;
import gestion.hospital.yurleidy.model.Usuario;
import gestion.hospital.yurleidy.repository.MedicoRepository;
import gestion.hospital.yurleidy.repository.UsuarioRepository;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(UsuarioRepository usuarioRepository, MedicoRepository medicoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.medicoRepository = medicoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin@clinica.com", "Admin", "Prueba", "ADMINISTRADOR");
        crearSiNoExiste("medico1@clinica.com", "Ana", "Torres", "MEDICO");
        crearSiNoExiste("medico2@clinica.com", "Luis", "Rojas", "MEDICO");
        crearSiNoExiste("paciente1@clinica.com", "Carlos", "Pérez", "PACIENTE");
        crearSiNoExiste("paciente2@clinica.com", "Laura", "Gómez", "PACIENTE");
    }

    private void crearSiNoExiste(String correo, String nombre, String apellido, String rol) {
        Usuario u = usuarioRepository.findByCorreo(correo).orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setApellido(apellido);
            nuevo.setCorreo(correo);
            nuevo.setContrasena(passwordEncoder.encode("1234"));
            nuevo.setTipoUsuario(rol);
            return usuarioRepository.save(nuevo);
        });

        // también queda en la tabla medico
        if (rol.equals("MEDICO") && !medicoRepository.existsByIdUsuario(u.getIdUsuario())) {
            Medico m = new Medico();
            m.setNombre(nombre + " " + apellido);
            m.setIdUsuario(u.getIdUsuario());
            medicoRepository.save(m);
        }
    }
}
