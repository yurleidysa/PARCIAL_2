DROP DATABASE IF EXISTS gestion_hospital;
CREATE DATABASE gestion_hospital CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestion_hospital;

CREATE TABLE usuario (
    id_usuario   INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(50)  NOT NULL,
    apellido     VARCHAR(50)  NOT NULL,
    correo       VARCHAR(100) NOT NULL UNIQUE,
    contrasena   VARCHAR(255) NOT NULL,
    tipo_usuario VARCHAR(15)  NOT NULL,
    CONSTRAINT chk_usuario_tipo CHECK (tipo_usuario IN ('ADMINISTRADOR', 'MEDICO', 'PACIENTE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla independiente de médicos; id_usuario une al médico con su cuenta
CREATE TABLE medico (
    id_medico  INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    id_usuario INT          NOT NULL UNIQUE,
    CONSTRAINT fk_medico_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE consulta (
    id_consulta        INT AUTO_INCREMENT PRIMARY KEY,
    nombre_paciente    VARCHAR(40)  NOT NULL,
    motivo             VARCHAR(100) NOT NULL,
    numero_consultorio INT          NOT NULL,
    hora_inicio        TIME         NOT NULL,
    hora_fin           TIME         NOT NULL,
    id_medico          INT          NOT NULL,
    CONSTRAINT fk_consulta_medico
        FOREIGN KEY (id_medico) REFERENCES medico (id_medico)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CREATE USER 'yur_ejercicios'@'localhost' IDENTIFIED BY '1234';
-- GRANT ALL PRIVILEGES ON gestion_hospital.* TO 'yur_ejercicios'@'localhost';
-- FLUSH PRIVILEGES;
