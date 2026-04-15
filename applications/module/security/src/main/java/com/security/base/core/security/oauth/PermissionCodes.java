package com.security.base.core.security.oauth;


import java.util.Set;
import java.util.regex.Pattern;


public final class PermissionCodes {

    private PermissionCodes() {
    }

    // Legacy compatibility privileges used by URL ACL seeders.
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    public static final String USER_READ = "user:read";
    public static final String USER_CREATE = "user:create";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";

    public static final String ROLE_READ = "role:read";
    public static final String ROLE_CREATE = "role:create";
    public static final String ROLE_UPDATE = "role:update";
    public static final String ROLE_DELETE = "role:delete";

    public static final String PRIVILEGE_READ = "privilege:read";
    public static final String PRIVILEGE_CREATE = "privilege:create";
    public static final String PRIVILEGE_UPDATE = "privilege:update";
    public static final String PRIVILEGE_DELETE = "privilege:delete";
    public static final String PRIVILEGE_ASSIGN = "privilege:assign";

    public static final String URL_READ = "url:read";
    public static final String URL_CREATE = "url:create";
    public static final String URL_UPDATE = "url:update";
    public static final String URL_DELETE = "url:delete";

    public static final String MATRIX_READ = "matrix:read";
    public static final String MATRIX_MANAGE = "matrix:manage";

    public static final String MODULE_READ = "module:read";
    public static final String MODULE_MANAGE = "module:manage";

    public static final String AMBULANCE_READ = "ambulance:read";
    public static final String AMBULANCE_CREATE = "ambulance:create";
    public static final String AMBULANCE_UPDATE = "ambulance:update";
    public static final String AMBULANCE_DELETE = "ambulance:delete";

    private static final Pattern MATRIX_PERMISSION_PATTERN =
            Pattern.compile("^[a-z][a-z0-9_-]*:[a-z][a-z0-9_-]*$");

    private static final Set<String> KNOWN_CODES = Set.of(
            ADMIN,
            USER,
            USER_READ,
            USER_CREATE,
            USER_UPDATE,
            USER_DELETE,
            ROLE_READ,
            ROLE_CREATE,
            ROLE_UPDATE,
            ROLE_DELETE,
            PRIVILEGE_READ,
            PRIVILEGE_CREATE,
            PRIVILEGE_UPDATE,
            PRIVILEGE_DELETE,
            PRIVILEGE_ASSIGN,
            URL_READ,
            URL_CREATE,
            URL_UPDATE,
            URL_DELETE,
            MATRIX_READ,
            MATRIX_MANAGE,
            MODULE_READ,
            MODULE_MANAGE,
            AMBULANCE_READ,
            AMBULANCE_CREATE,
            AMBULANCE_UPDATE,
            AMBULANCE_DELETE
    );

    public static boolean isKnownCode(final String value) {
        return value != null && KNOWN_CODES.contains(value);
    }

    public static boolean isLegacyCode(final String value) {
        return ADMIN.equals(value) || USER.equals(value);
    }

    public static boolean isMatrixCode(final String value) {
        return value != null && MATRIX_PERMISSION_PATTERN.matcher(value).matches();
    }

    public static boolean isSupportedPermissionCode(final String value) {
        return isLegacyCode(value) || isMatrixCode(value);
    }
}

