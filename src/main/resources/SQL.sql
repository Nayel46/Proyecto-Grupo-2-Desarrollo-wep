
CREATE DATABASE IF NOT EXISTS `healthnow`
/*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */
/*!80016 DEFAULT ENCRYPTION='N' */;

USE `healthnow`;

-- MySQL dump 10.13 Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: healthnow
-- ------------------------------------------------------
-- Server version 8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- ============================================
-- TABLA USUARIO
-- CÓDIGO ORIGINAL DEL COMPAÑERO
-- ============================================

-- Se conserva la tabla si ya existe.
-- No se eliminan usuarios registrados.

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;

CREATE TABLE IF NOT EXISTS `usuario` (

  `id_usuario` int NOT NULL AUTO_INCREMENT,

  `activo` bit(1) NOT NULL,

  `apellidos` varchar(100)
      COLLATE utf8mb4_unicode_ci NOT NULL,

  `cedula` varchar(30)
      COLLATE utf8mb4_unicode_ci NOT NULL,

  `correo` varchar(150)
      COLLATE utf8mb4_unicode_ci NOT NULL,

  `nombre` varchar(60)
      COLLATE utf8mb4_unicode_ci NOT NULL,

  `password` varchar(255)
      COLLATE utf8mb4_unicode_ci NOT NULL,

  `telefono` varchar(25)
      COLLATE utf8mb4_unicode_ci DEFAULT NULL,

  `username` varchar(30)
      COLLATE utf8mb4_unicode_ci NOT NULL,

  -- Campo para los roles
  `rol` varchar(20)
      COLLATE utf8mb4_unicode_ci
      NOT NULL DEFAULT 'PACIENTE',

  PRIMARY KEY (`id_usuario`),

  UNIQUE KEY `UK6lpw46du147baun4iyme6m6uk`
      (`cedula`),

  UNIQUE KEY `UK2mlfr087gb1ce55f2j87o74t`
      (`correo`),

  UNIQUE KEY `UK863n1y3x0jalatoir4325ehal`
      (`username`),

  CONSTRAINT `chk_usuario_rol`
      CHECK (`rol` IN ('ADMIN', 'DOCTOR', 'PACIENTE'))

) ENGINE=InnoDB
AUTO_INCREMENT=2
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;

/*!40101 SET character_set_client = @saved_cs_client */;


INSERT INTO `usuario` (

    `activo`,
    `apellidos`,
    `cedula`,
    `correo`,
    `nombre`,
    `password`,
    `telefono`,
    `username`,
    `rol`

)
SELECT

    b'1',
    'Sistema',
    'ADMIN001',
    'admin@healthnow.local',
    'Administrador',
    '$2a$10$CyaPKlgGlhV0BvphWBGyreOSBRZOu.XN4FfMh4ScXO.8Gc6/FwVUe',
    NULL,
    'admin',
    'ADMIN'

WHERE NOT EXISTS (

    SELECT 1
    FROM `usuario`
    WHERE `username` = 'admin'

);


CREATE TABLE IF NOT EXISTS `especialidad` (

    `id_especialidad` INT NOT NULL AUTO_INCREMENT,

    `nombre` VARCHAR(100)
        COLLATE utf8mb4_unicode_ci NOT NULL,

    `comentarios` VARCHAR(500)
        COLLATE utf8mb4_unicode_ci DEFAULT NULL,

    `activo` BIT(1) NOT NULL DEFAULT b'1',

    PRIMARY KEY (`id_especialidad`),

    UNIQUE KEY `uk_especialidad_nombre`
        (`nombre`)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- ============================================
-- ESPECIALIDADES INICIALES
-- ============================================

INSERT INTO `especialidad`
    (`nombre`, `comentarios`, `activo`)
VALUES

(
    'Pediatría',
    'Atención médica para niños y adolescentes',
    b'1'
),

(
    'Medicina General',
    'Atención médica general para pacientes',
    b'1'
),

(
    'Cardiología',
    'Diagnóstico y tratamiento de enfermedades del corazón',
    b'1'
),

(
    'Dermatología',
    'Atención de enfermedades de la piel',
    b'1'
),

(
    'Ginecología',
    'Atención de la salud ginecológica',
    b'1'
),

(
    'Neurología',
    'Atención de enfermedades del sistema nervioso',
    b'1'
),

(
    'Ortopedia',
    'Atención de problemas de huesos y articulaciones',
    b'1'
),

(
    'Oftalmología',
    'Atención de la salud visual',
    b'1'
),

(
    'Odontología',
    'Atención de la salud bucodental',
    b'1'
),

(
    'Psicología',
    'Atención y acompañamiento psicológico',
    b'1'
)

ON DUPLICATE KEY UPDATE
    `id_especialidad` = `id_especialidad`;


-- ============================================
-- MÉDICO INICIAL DEL SISTEMA
-- ============================================

INSERT INTO usuario (
    activo,
    apellidos,
    cedula,
    correo,
    nombre,
    password,
    telefono,
    username,
    rol
)
SELECT
    b'1',
    'Rodríguez Vargas',
    'MED001',
    'medico@healthnow.local',
    'Carlos',
    '$2a$10$CyaPKlgGlhV0BvphWBGyreOSBRZOu.XN4FfMh4ScXO.8Gc6/FwVUe',
    '8888-0000',
    'medico',
    'DOCTOR'
WHERE NOT EXISTS (
    SELECT 1
    FROM usuario
    WHERE username = 'medico'
);



CREATE TABLE IF NOT EXISTS `medico` (

    `id_medico` INT NOT NULL AUTO_INCREMENT,

    `id_usuario` INT NOT NULL,

    `id_especialidad` INT NOT NULL,

    `descripcion` VARCHAR(500) DEFAULT NULL,

    PRIMARY KEY (`id_medico`),

    UNIQUE KEY `uk_medico_usuario` (`id_usuario`),

    CONSTRAINT `fk_medico_usuario`
        FOREIGN KEY (`id_usuario`)
        REFERENCES `usuario` (`id_usuario`),

    CONSTRAINT `fk_medico_especialidad`
        FOREIGN KEY (`id_especialidad`)
        REFERENCES `especialidad` (`id_especialidad`)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;

INSERT INTO `medico` (
    `id_usuario`,
    `id_especialidad`,
    `descripcion`
)
SELECT
    u.id_usuario,
    e.id_especialidad,
    'Médico especializado en atención pediátrica'
FROM usuario u
INNER JOIN especialidad e
    ON e.nombre = 'Pediatría'
WHERE u.username = 'medico'
  AND u.rol = 'DOCTOR'
  AND NOT EXISTS (
      SELECT 1
      FROM medico m
      WHERE m.id_usuario = u.id_usuario
  );


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


USE healthnow;

-- Verificar las tablas

SHOW TABLES;


-- Consultar usuarios registrados

SELECT
    id_usuario,
    username,
    nombre,
    apellidos,
    correo,
    rol,
    activo
FROM usuario;


-- Verificar administrador

SELECT
    id_usuario,
    username,
    rol,
    activo
FROM usuario
WHERE username = 'admin';


-- Consultar pacientes

SELECT
    id_usuario,
    username,
    rol
FROM usuario
WHERE rol = 'PACIENTE';


-- Consultar doctores

SELECT
    id_usuario,
    username,
    rol
FROM usuario
WHERE rol = 'DOCTOR';


-- Consultar todas las especialidades

SELECT
    id_especialidad,
    nombre,
    comentarios,
    activo
FROM especialidad;


-- Consultar especialidades activas

SELECT
    id_especialidad,
    nombre,
    comentarios
FROM especialidad
WHERE activo = b'1';


-- Contar especialidades activas

SELECT
    COUNT(*) AS total_especialidades
FROM especialidad
WHERE activo = b'1';


-- ============================================
-- CONSULTAR MÉDICOS CON SUS ESPECIALIDADES
-- ============================================

SELECT
    m.id_medico,
    u.nombre,
    u.apellidos,
    u.username,
    e.nombre AS especialidad,
    m.descripcion
FROM medico m
INNER JOIN usuario u
    ON m.id_usuario = u.id_usuario
INNER JOIN especialidad e
    ON m.id_especialidad = e.id_especialidad
WHERE u.rol = 'DOCTOR';


-- ============================================
-- CONSULTAR MÉDICOS DE PEDIATRÍA
-- ============================================

SELECT
    m.id_medico,
    u.nombre,
    u.apellidos,
    e.nombre AS especialidad,
    m.descripcion
FROM medico m
INNER JOIN usuario u
    ON m.id_usuario = u.id_usuario
INNER JOIN especialidad e
    ON m.id_especialidad = e.id_especialidad
WHERE e.nombre = 'Pediatría'
  AND e.activo = b'1'
  AND u.activo = b'1'
  AND u.rol = 'DOCTOR';

