package com.example.DecorEcommerceProject.Service;

import javax.mail.MessagingException;

public interface IEmailService {
    void sendEmail(String to, String subject, String content) throws MessagingException;
}
