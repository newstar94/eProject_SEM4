package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Service.IEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender javaMailSender;
    @Autowired
    public EmailServiceImpl (JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    @Override
    public void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        javaMailSender.send(message);
    }
}
