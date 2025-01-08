package com.hmtmcse.v3base.model.enums;

public enum Permissions {
    ADMINISTRATION("Administration"),
    BASIC("Basic");

    public String label;

    Permissions(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }


    public static Permissions fromLabel(String label) {
        for (Permissions permission : Permissions.values()) {
            if (permission.label.equalsIgnoreCase(label)) {
                return permission;
            }
        }
        return Permissions.BASIC;
    }


}
