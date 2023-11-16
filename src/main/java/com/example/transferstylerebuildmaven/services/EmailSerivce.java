package com.example.transferstylerebuildmaven.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;


@Service
public class EmailSerivce {


    private static final Logger logger = LoggerFactory
            .getLogger(EmailSerivce.class);

    private final JavaMailSender javaMailSender;

    public EmailSerivce(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendTextEmail(String destination, String subject, String message ) {
        logger.info("Simple Email sending start");

        SimpleMailMessage simpleMessage = new SimpleMailMessage();
        simpleMessage.setTo(destination);
        simpleMessage.setSubject(subject);
        simpleMessage.setText(message);

        javaMailSender.send(simpleMessage);

        logger.info("Simple Email sent");

    }

    public void sendEmailWithAttachment(String destination, String subject, String message, File file) {
        logger.info("Sending email with attachment start");

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            // Set multipart mime message true
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                    true);
            mimeMessageHelper.setTo(destination);
            mimeMessageHelper
                    .setSubject(subject);
            mimeMessageHelper.setText(message);

            // Attach the attachment
            mimeMessageHelper.addAttachment("Result_files.zip",
                   file);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            logger.error("Exeception=>sendEmailWithAttachment ", e);
        }

        logger.info("Email with attachment sent");
    }
}
