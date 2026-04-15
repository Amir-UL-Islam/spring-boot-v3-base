package com.security.base.core.security.mfa.otp.provider;

import com.security.base.core.security.mfa.MfaFactorType;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailOtpDeliveryProvider implements OtpDeliveryProvider {

    private final JavaMailSender javaMailSender;
    private final OtpEmailProperties otpEmailProperties;

    @Override
    public MfaFactorType factor() {
        return MfaFactorType.EMAIL;
    }

    @Override
    public void sendOtp(final String destination, final String message) {
        final SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(otpEmailProperties.getFrom());
        mail.setTo(destination);
        mail.setSubject(otpEmailProperties.getSubjectPrefix() + " Verification Code");
        mail.setText(message);
        javaMailSender.send(mail);
    }
}

