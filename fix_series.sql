-- Script para actualizar la BD existente
-- Ejecutar en MySQL antes de arrancar la aplicación

-- 1. Permitir NULL en ejercicio_id
ALTER TABLE serie_realizada MODIFY COLUMN ejercicio_id BIGINT NULL;

-- 2. Añadir columna nombre_ejercicio si no existe
ALTER TABLE serie_realizada ADD COLUMN IF NOT EXISTS nombre_ejercicio VARCHAR(255);

-- 3. Rellenar nombre_ejercicio para series existentes que aun tienen ejercicio
UPDATE serie_realizada sr
INNER JOIN ejercicio e ON sr.ejercicio_id = e.id
SET sr.nombre_ejercicio = e.nombre
WHERE sr.nombre_ejercicio IS NULL;
