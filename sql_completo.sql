-- actualizado a 24-05



-- 1. Reinicio total de la base de datos
DROP DATABASE IF EXISTS gimnasio_db;
CREATE DATABASE gimnasio_db;
USE gimnasio_db;

-- 2. Creación de tablas con tipos BIGINT (para evitar errores con Java Long)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    entrenador_id BIGINT,
    CONSTRAINT fk_entrenador FOREIGN KEY (entrenador_id) REFERENCES usuarios(id)
);

CREATE TABLE usuarios_roles (
    usuario_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, role_id),
    CONSTRAINT fk_user_role FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_role_user FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 3. Nueva tabla: Ejercicios (definición de ejercicios disponibles)
CREATE TABLE ejercicio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    grupo_muscular VARCHAR(50),
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ejercicio_usuario FOREIGN KEY (user_id) REFERENCES usuarios(id)
);

-- 4. Nueva tabla: Series Realizadas (registro de cada serie de cada ejercicio)
CREATE TABLE serie_realizada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    ejercicio_id BIGINT,
    nombre_ejercicio VARCHAR(255), -- ¡NUEVO!: Almacena el nombre para no perderlo en el historial si se borra
    fecha DATE NOT NULL,
    numero_serie INT NOT NULL,
    repeticiones INT NOT NULL,
    peso DECIMAL(5, 2),
    notas TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_serie_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_serie_ejercicio FOREIGN KEY (ejercicio_id) REFERENCES ejercicio(id) ON DELETE SET NULL,
    INDEX idx_usuario_fecha (usuario_id, fecha),
    INDEX idx_ejercicio_usuario (ejercicio_id, usuario_id)
);

-- 5. NUEVA TABLA: Historial de Notificaciones de Cambios del Entrenador
CREATE TABLE notificacion_cambio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    entrenador_id BIGINT NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notif_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_entrenador FOREIGN KEY (entrenador_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 6. Nueva tabla: Contrato Entrenador (Para controlar el tiempo de duración)
CREATE TABLE contrato_entrenador (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    entrenador_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    CONSTRAINT fk_contrato_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_contrato_entrenador FOREIGN KEY (entrenador_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- 7. Inserción de datos maestros

-- Insertamos roles con el prefijo ROLE_ (estándar de Spring Security)
INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_USER');



-- 8. Insertar ejercicios predefinidos del sistema (Globales)
INSERT INTO ejercicio (nombre, descripcion, grupo_muscular) VALUES
('Press de Banca', 'Ejercicio de pecho con barra', 'Pecho'),
('Sentadillas', 'Ejercicio de piernas con barra', 'Piernas'),
('Peso Muerto', 'Levantamiento de peso muerto', 'Espalda'),
('Flexiones', 'Ejercicio de pecho con peso corporal', 'Pecho'),
('Dominadas', 'Ejercicio de espalda con peso corporal', 'Espalda'),
('Polea al Pecho', 'Ejercicio de espalda con polea', 'Espalda'),
('Encogimiento de Hombros', 'Ejercicio de hombros con mancuernas', 'Hombros'),
('Curl de Bíceps', 'Ejercicio de bíceps con mancuernas', 'Bíceps'),
('Extensión de Tríceps', 'Ejercicio de tríceps con cable', 'Tríceps');

