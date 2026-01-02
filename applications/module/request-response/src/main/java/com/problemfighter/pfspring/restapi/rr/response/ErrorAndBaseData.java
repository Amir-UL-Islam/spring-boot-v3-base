package com.problemfighter.pfspring.restapi.rr.response;

import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorAndBaseData extends BaseData {
    public ErrorData error;

    public void initErrorData() {
        if (this.error == null) {
            this.error = new ErrorData();
        }

    }

    public void addErrorMessage(String message) {
        this.initErrorData();
        this.error.message = (new I18nMessage(message)).setTextToKey(message);
    }

    public void addErrorReason(String key, String explanation) {
        this.initErrorData();
        if (this.error.details == null) {
            this.error.details = new LinkedHashMap();
        }

        this.error.details.put(key, (new I18nMessage(explanation)).setTextToKey(explanation));
    }

    public void addI18nReason(LinkedHashMap<String, String> details) {
        for(Map.Entry<String, String> entry : details.entrySet()) {
            this.addErrorReason((String)entry.getKey(), (String)entry.getValue());
        }

    }

    public void updateErrorMessageKey(String key) {
        if (this.error != null && this.error.message != null) {
            this.error.message.key = key;
        }

    }
}
