package com.fantion.backend.common.component;

import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
public class MailComponents {

    private final JavaMailSender javaMailSender;

    public void sendMail(String email, String title, String text) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true, "UTF-8");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(title);
            mimeMessageHelper.setText(text, true);

        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.SEND_MAIL_FAIL);
        }

        javaMailSender.send(message);
    }
}
