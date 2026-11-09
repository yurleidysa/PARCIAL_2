package gestion.hospital.yurleidy.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import gestion.hospital.yurleidy.repository.UsuarioRepository;

@Controller
public class PaginaController {

    private final UsuarioRepository usuarioRepository;

    public PaginaController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/acceso-denegado")
    public String accesoDenegado(Authentication auth, Model model) {
        agregarNombre(auth, model);
        return "acceso-denegado";
    }

    // según el rol, cada usuario va a su vista
    @GetMapping("/")
    public String inicio(Authentication auth) {
        String rol = auth.getAuthorities().iterator().next().getAuthority();
        return switch (rol) {
            case "ROLE_ADMINISTRADOR" -> "redirect:/admin";
            case "ROLE_MEDICO" -> "redirect:/medico";
            default -> "redirect:/paciente";
        };
    }

    @GetMapping("/admin")
    public String admin(Authentication auth, Model model) {
        agregarNombre(auth, model);
        return "admin";
    }

    @GetMapping("/medico")
    public String medico(Authentication auth, Model model) {
        agregarNombre(auth, model);
        return "medico";
    }

    @GetMapping("/paciente")
    public String paciente(Authentication auth, Model model) {
        agregarNombre(auth, model);
        return "paciente";
    }

    // en la barra de cada vista se muestra el nombre, no el correo
    private void agregarNombre(Authentication auth, Model model) {
        if (auth == null) return;
        usuarioRepository.findByCorreo(auth.getName())
                .ifPresent(u -> model.addAttribute("nombreUsuario", u.getNombre() + " " + u.getApellido()));
    }
}
