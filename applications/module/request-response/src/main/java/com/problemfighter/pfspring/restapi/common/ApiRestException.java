package com.problemfighter.pfspring.restapi.common;

import com.problemfighter.pfspring.restapi.rr.ResponseProcessor;

public class ApiRestException extends RuntimeException {
    public Object errorMessage;

    public ApiRestException() {
        super("Api Processor Exception");
    }

    public ApiRestException(String message) {
        super(message);
    }

    public ApiRestException error(Object errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ApiRestException errorException(String message) {
        this.error((Object)ResponseProcessor.errorMessage(message));
        return this;
    }

    public Object getError() {
        return this.errorMessage == null ? ResponseProcessor.unknownError() : this.errorMessage;
    }

    public static void throwException(Object errorMessage) throws ApiRestException {
        throw (new ApiRestException()).error(errorMessage);
    }

    public static void notFound(String message) {
        throwException(ResponseProcessor.notFound(message));
    }

    public static void notFound() {
        throwException(ResponseProcessor.notFound());
    }

    public static void unauthorized() {
        throwException(ResponseProcessor.unauthorized());
    }

    public static void unauthorized(String errorMessage) {
        throwException(ResponseProcessor.unauthorized(errorMessage));
    }

    public static void otherError(String errorMessage) {
        throwException(ResponseProcessor.otherError(errorMessage));
    }

    public static void error(String message) {
        throwException(ResponseProcessor.errorMessage(message));
    }
}
