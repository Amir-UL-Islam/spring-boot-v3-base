package com.hmtmcse.security.exceptions;

public class NotFoundException extends Throwable {
    public NotFoundException() {
        super();
    }

    public NotFoundException(String s) {
        super(s);
    }

    public NotFoundException(String s, Throwable e) {
        super(s, e);
    }
}
