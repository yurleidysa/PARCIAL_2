package gestion.hospital.yurleidy.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// devuelve mensajes de error claros cuando los datos enviados no son válidos
@RestControllerAdvice
public class ManejoErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(". "));
        return respuesta(mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> formatoInvalido(HttpMessageNotReadableException ex) {
        return respuesta("Formato de datos incorrecto. Las horas van como \"08:30\" y los números sin comillas");
    }

    private ResponseEntity<Map<String, Object>> respuesta(String mensaje) {
        return ResponseEntity.badRequest()
                .body(Map.of("status", 400, "error", "Bad Request", "message", mensaje));
    }
}
