package com.problemfighter.pfspring.restapi.rr.response;

import java.io.Serializable;

public class BaseData implements Serializable {
    public Status status;
    public String code;

    public void success() {
        this.code = "1200";
        this.status = Status.success;
    }

    public void error() {
        this.code = "1510";
        this.status = Status.error;
    }
}
