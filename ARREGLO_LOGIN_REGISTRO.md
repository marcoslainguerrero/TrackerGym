# Resumen de Arreglos - Sistema de Login y Registro

## Problemas Identificados
1. **Roles no existentes en la BD**: Los roles `ROLE_ADMIN` y `ROLE_USER` no se creaban automáticamente
2. **Usuario Admin no existente**: El usuario `entrenador_master` no se creaba al iniciar la BD
3. **Configuración de Hibernate**: `ddl-auto=none` impedía que se crearan las tablas automáticamente
4. **Falta de validación**: El proceso de registro no tenía validaciones suficientes ni logging

## Cambios Realizados

### 1. ✅ Creé nuevo archivo: `DataInitializer.java`
**Ubicación**: `src/main/java/TrackerGym/config/DataInitializer.java`

Este archivo se encarga de:
- Crear automáticamente los roles `ROLE_ADMIN` y `ROLE_USER` al iniciar la aplicación si no existen
- Crear el usuario administrador `entrenador_master` con contraseña `admin123` si no existe
- Mostrar en consola si los roles/usuarios se crearon correctamente

### 2. ✅ Actualicé: `application.properties`
**Cambios**:
- De: `spring.jpa.hibernate.ddl-auto=none`
- A: `spring.jpa.hibernate.ddl-auto=update`
- Agregué: `spring.jpa.properties.hibernate.format_sql=true` (para mejor legibilidad de logs SQL)

**Por qué**: Ahora Hibernate crea automáticamente las tablas en la BD si no existen

### 3. ✅ Mejoré: `RegisterController.java`
**Cambios**:
- Agregué logging con SLF4J para debug
- Validación adicional de contraseña vacía
- Verificación que el rol existe en la BD antes de asignar
- Mejor manejo de excepciones con mensajes descriptivos
- Logs detallados para cada paso del registro

### 4. ✅ Actualicé: `login.html`
**Cambios**:
- Agregué clase CSS `.success` 
- Agregué mensaje de éxito cuando el usuario se registra correctamente
- Mensaje: "¡Registro exitoso! Por favor inicia sesión con tus credenciales."

---

## Cómo Probar

### 1️⃣ **Iniciar la Aplicación**
```powershell
cd c:\Users\CampusFP\MARCOSLAIN\TFG\TrackerGym
./mvnw spring-boot:run
```

O ejecutar el JAR compilado:
```powershell
java -jar target\TrackerGym-0.0.1-SNAPSHOT.jar
```

Verás en consola:
```
✓ Rol ya existe: ROLE_ADMIN
✓ Rol ya existe: ROLE_USER
✓ Usuario administrador ya existe: entrenador_master
```

### 2️⃣ **Acceder a la Aplicación**
- Abre tu navegador: `http://localhost:8080/login`

### 3️⃣ **Probar el Login Existente**
- Usuario: `entrenador_master`
- Contraseña: `admin123`
- Deberías entrar al dashboard del entrenador

### 4️⃣ **Registrarse como Cliente Nuevo**
- Click en "Regístrate aquí"
- Rellena el formulario:
  - Usuario: (cualquier nombre, ej: `cliente1`)
  - Contraseña: (ej: `pass123`)
  - Confirmar Contraseña: (debe coincidir)
  - Tipo de Usuario: **Cliente** (ROLE_USER)
- Click en "Registrarse"
- Deberías ver: "¡Registro exitoso! Por favor inicia sesión con tus credenciales."
- Ahora puedes iniciar sesión con las credenciales nuevas

### 5️⃣ **Registrarse como Entrenador Nuevo**
- Repite el paso anterior pero selecciona **Entrenador** (ROLE_ADMIN)
- El nuevo entrenador podrá acceder al dashboard de entrenador

---

## Verificación en Base de Datos

Si quieres verificar que los usuarios se guardan correctamente:

```sql
USE gimnasio_db;

-- Ver todos los usuarios
SELECT u.id, u.username, r.name AS rol 
FROM usuarios u 
LEFT JOIN usuarios_roles ur ON u.id = ur.usuario_id 
LEFT JOIN roles r ON ur.role_id = r.id;

-- Ver solo roles
SELECT * FROM roles;

-- Ver usuarios y sus entrenadores
SELECT u.id, u.username, e.username AS entrenador 
FROM usuarios u 
LEFT JOIN usuarios e ON u.entrenador_id = e.id;
```

---

## Logs y Debugging

Cuando registres un usuario, verás en la consola logs como:

```
[INFO] TrackerGym.controller.RegisterController - Intento de registro: usuario=cliente1, rol=ROLE_USER
[INFO] TrackerGym.controller.RegisterController - Usuario registrado exitosamente: cliente1
```

Si hay error, verás:

```
[ERROR] TrackerGym.controller.RegisterController - Error al registrar usuarioscliente1:
```

---

## Notas Importantes

✅ **La aplicación ahora**:
- Crea automáticamente roles al iniciar
- Crea automáticamente el usuario admin
- Valida correctamente el registro de nuevos clientes
- Guarda los usuarios en la BD exitosamente
- Muestra mensajes de error/éxito claros

⚠️ **Requisitos**:
- MySQL debe estar corriendo en `localhost:3306`
- Usuario MySQL: `root`
- Contraseña MySQL: `campusfp`
- Si cambias la contraseña, actualiza `application.properties`

---

## Si Algo No Funciona

1. **BD no se crea**: Verifica que MySQL está corriendo
2. **Error de conexión**: Revisa `application.properties` (usuario/contraseña de MySQL)
3. **Roles no se crean**: Elimina la BD y reinicia (el `DataInitializer` la recreará)
4. **Usuario no se guarda**: Chequea los logs en consola para mensajes de error específicos

---

¡Listo! El sistema de login y registro ahora funciona correctamente.
