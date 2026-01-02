package com.problemfighter.pfspring.restapi.exception;

import com.problemfighter.pfspring.restapi.rr.ResponseProcessor;
import com.problemfighter.pfspring.restapi.rr.response.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public class HttpToApiException {
    private int getResponseCode(HttpServletRequest request) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        return status != null ? Integer.parseInt(status.toString()) : 0;
    }

    public MessageResponse processError(HttpServletRequest request) {
        int statusCode = this.getResponseCode(request);
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return ResponseProcessor.notFound();
        } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            return ResponseProcessor.badRequest();
        } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
            return ResponseProcessor.unauthorized();
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return ResponseProcessor.forbidden();
        } else {
            return statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value() ? ResponseProcessor.codeError() : ResponseProcessor.unknownError();
        }
    }

    public static MessageResponse handleException(HttpServletRequest request) {
        return (new HttpToApiException()).processError(request);
    }
}
