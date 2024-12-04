/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import javax.enterprise.context.Dependent;
import javax.inject.Named;
@Named
@Dependent // or appropriate scope
public class EmailService {

    public void sendEmail(String to, String subject, String authCode) {
        String from = ""; // Email pengirim
        String host = "smtp.gmail.com";
        final String username = ""; // Email pengirim
        final String password = ""; 

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587"); // Port untuk TLS
        properties.put("mail.smtp.starttls.enable", "true"); // Menggunakan TLS
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password); // Kredensial email pengirim
            }
        });

        try {
            // Membuat pesan email
            String content = "Yah kamu lupa password ya?, "
                    + "Ini kode OTP buat Kamu ya...\n\n"
                     + authCode + "\n\n"
                    + "Kode OTP hanya berlaku 10 menit dan bersifat rahasia. Mohon untuk tidak membagikan kode ini kepada siapapun termasuk pihak yang mengatasnamakan Belanjain.\n\n"
                    + "Email ini dibuat otomatis, mohon untuk tidak membalas.";

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(content);

            // Mengirim email
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
