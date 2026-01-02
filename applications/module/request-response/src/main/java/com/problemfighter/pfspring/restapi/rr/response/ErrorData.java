package com.problemfighter.pfspring.restapi.rr.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.LinkedHashMap;

@JsonInclude(Include.NON_NULL)
public class ErrorData implements Serializable {
    public I18nMessage message;
    public LinkedHashMap<String, I18nMessage> details;
}
