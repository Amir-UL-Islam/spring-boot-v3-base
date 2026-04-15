package com.security.base.core.security.mfa.otp.provider;

import com.security.base.core.security.mfa.MfaFactorType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsOtpDeliveryProvider implements OtpDeliveryProvider {

    private final MimsmsProperties mimsmsProperties;
    private final RestClient restClient = RestClient.builder().build();

    @Override
    public MfaFactorType factor() {
        return MfaFactorType.SMS;
    }

    @Override
    public void sendOtp(final String destination, final String message) {
        if (!StringUtils.hasText(mimsmsProperties.getApi())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "SMS provider is not configured");
        }

        restClient.post()
                .uri(mimsmsProperties.getApi())
                .body(Map.of(
                        "apikey", safe(mimsmsProperties.getApiKey()),
                        "secretkey", safe(mimsmsProperties.getApiSecret()),
                        "callerID", safe(mimsmsProperties.getSenderId()),
                        "toUser", destination,
                        "messageContent", message,
                        "username", safe(mimsmsProperties.getUsername()),
                        "password", safe(mimsmsProperties.getPassword())
                ))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.warn("sms provider returned status {}", response.getStatusCode());
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to deliver SMS OTP");
                })
                .toBodilessEntity();
    }

    private String safe(final String value) {
        return value == null ? "" : value;
    }
}

