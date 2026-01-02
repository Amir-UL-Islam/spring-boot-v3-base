package com.problemfighter.pfspring.restapi.rr.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;

@JsonInclude(Include.NON_NULL)
public class I18nMessage implements Serializable {
    public String text;
    public String key;

//    TODO: Will Implement Later
//    public String code;

    public I18nMessage() {
    }

    public I18nMessage(String text) {
        this.text = text;
    }

    public I18nMessage(String text, String key) {
        this.text = text;
        this.key = key;
    }

    public I18nMessage setText(String text) {
        this.text = text;
        return this;
    }

    public I18nMessage setKey(String key) {
        this.key = key;
        return this;
    }

    public I18nMessage setTextToKey(String text) {
        this.key = this.textToKey(text);
        return this;
    }

    public String textToKey(String text) {
        if (text != null && !text.isBlank() && !text.isEmpty()) {
            text = text.toLowerCase();
            text = text.replace(" ", ".");
            return text;
        } else {
            return null;
        }
    }

    public static I18nMessage message(String text) {
        return text == null ? null : (new I18nMessage(text)).setTextToKey(text);
    }
}
