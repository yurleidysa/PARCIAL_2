# Gestión de Consultas Médicas

Tecnologías usadas: Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA, MySQL 8, Thymeleaf, Bootstrap 5 y Swagger.

## Requisitos
- Java 21
- MySQL 8

## Cómo ejecutarlo

1. Ejecute el script `base_de_datos/script.sql` en MySQL. Crea la base `gestion_hospital` y sus tablas 

2. Usuario de MySQL: la aplicación usa `yur_ejercicios` / `1234`. Al final del script están comentadas las líneas para crearlo y darle permisos sobre `gestion_hospital`. Si no cambialo por otro
3. Desde la carpeta del proyecto ejecute: `mvnw spring-boot:run`
4. Abra `http://localhost:8080`.

## Cuentas de demostración
- Administrador: `admin@clinica.com`
- Médico: `medico1@clinica.com` (Ana Torres)
- Médico: `medico2@clinica.com` (Luis Rojas)
- Paciente: `paciente1@clinica.com` (Carlos Pérez)

Si un usuario intenta entrar a una vista o acción sin permiso, se muestra la vista personalizada de acceso denegado.

## Swagger

Documentación y pruebas de los servicios web en `http://localhost:8080/swagger-ui.html`.

Primero inicie sesión en el mismo navegador; las pruebas usan esa sesión y sus permisos.

- **200 OK:** la acción se realizó.
- **401 Unauthorized:** no se ha iniciado sesión.
- **403 Forbidden:** el rol no tiene permiso para esa acción.
