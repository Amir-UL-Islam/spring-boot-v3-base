package com.security.base.core.security.oauth;


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
}

