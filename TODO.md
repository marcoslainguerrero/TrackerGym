# TODO: Add Client Registration Section to Admin Home

## Tasks
- [x] Modify `src/main/resources/templates/admin/home.html` to add a new card titled "Registrar Nuevo Cliente" with a link to `/admin/clientes/nuevo`.
- [x] Add imports and new mappings in `src/main/java/TrackerGym/controller/ControladorAdmin.java` for GET and POST `/admin/clientes/nuevo`.
- [x] Create a new template `src/main/resources/templates/admin/registrar-cliente.html` with a form for entering client details (username, password, and optionally select a trainer).
- [x] Update `ServicioUsuarios.java` to include methods for obtaining trainers and registering clients with role assignment and password encoding.
- [x] Ensure the form redirects back to the client list after successful registration.
- [x] Update `src/main/resources/templates/admin/lista-clientes.html` to include success/error messages and a link to register new client.
