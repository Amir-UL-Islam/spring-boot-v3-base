package com.problemfighter.java.oc.common;

public interface ProcessCustomCopy<E, D> {
    void meAsSrc(D var1, E var2);

    void meAsDst(E var1, D var2);

    default void csvExport(E source, E destination) {
    }

    default void csvImport(D source, E destination) {
    }

    default void whyNotCalled(String message) {
    }
}
