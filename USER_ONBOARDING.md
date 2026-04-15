# User Onboarding (Security Matrix)

## Purpose
This guide explains how to onboard users in the development security model with matrix permissions (`resource:action`).

## Seeded Users (Development)
The system seeds these users on startup:

- `superadmin` / `superadmin123!`
- `admin` / `admin123!`
- `user` / `user123!`

## Role Model
- `SUPER_ADMIN`: full access to all privileges.
- `ADMIN`: operational admin access (user management + read access across role/privilege/url/module matrix).
- `USER`: least-privilege baseline for normal authenticated usage.

## Permission Naming Convention
Use lower-case matrix permissions:

- `user:read`, `user:create`, `user:update`, `user:delete`
- `role:read`, `role:create`, `role:update`, `role:delete`
- `privilege:read`, `privilege:create`, `privilege:update`, `privilege:delete`, `privilege:assign`
- `url:read`, `url:create`, `url:update`, `url:delete`
- `matrix:read`, `matrix:manage`
- `module:read`, `module:manage`

## Login and Matrix Verification
1. Authenticate with `POST /authenticate`.
2. Call `GET /api/me/permissions` with bearer token.
3. Confirm:
   - `roles` includes expected role names.
   - `authorities` includes expected `resource:action` codes.
   - `matrix` groups authority actions by resource.

## Admin Console
- URL: `/assets/admin/index.html`
- Superadmin/admin users get full admin screens according to permissions.
- Non-admin users get matrix-only view.

## Onboarding a New User (API)
1. Login as `superadmin` or `admin`.
2. Fetch role values from `GET /api/user/roleValues`.
3. Create user via `POST /api/user` with role IDs.
4. Validate effective permissions with the new account using `GET /api/me/permissions`.

## Adding a New Permission to the System
1. Add code constant in `PermissionCodes`.
2. Seed privilege in `PrivilegeLoader`.
3. Assign to role(s) in `RoleLoader`.
4. Map endpoint+method in `UrlsLoader`.
5. Enforce in controller/service using `@PreAuthorize("hasAuthority('resource:action')")`.
6. Surface behavior in admin UI (`/assets/admin/app.js`) using `permissions.can("resource:action")`.

## Troubleshooting
- `403 Access Denied`: check URL mapping (`/api/url`) and role privilege assignment.
- Missing matrix rows: verify privilege exists and user role includes it.
- Authenticated but missing actions in UI: check `/api/me/permissions` response first.

