# Security Admin Console (Dev)

This lightweight UI is served by Spring Boot at:

- `/assets/admin/index.html`

## Scope

- Login via `/authenticate`
- Fetch current permission matrix from `/api/me/permissions`
- Render role/authority matrix per resource/action
- Manage Users (`/api/user`)
- Manage Roles (`/api/roles`)
- Manage Privileges (`/api/privileges`)
- Manage URL mappings (`/api/url`)
- View policy drift diagnostics (`/api/policy/drift`)

## Seeded dev users

- `superadmin / superadmin123!`
- `admin / admin123!`
- `user / user123!`

These are created by `UserLoader` and intended for development mode only.

## Permission-driven behavior

- UI action visibility is controlled by `permissions.can("resource:action")`.
- Superadmin/admin receive management sections according to authorities.
- Non-admin roles can sign in and view their matrix but do not get full management screens.
- Drift diagnostics are visible only with `matrix:manage` authority.

