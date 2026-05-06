package com.aiimage.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

@Component
@Slf4j
public class EmailUtil {

    @Value("${app.email.smtp-host}")
    private String smtpHost;

    @Value("${app.email.smtp-port}")
    private int smtpPort;

    @Value("${app.email.username}")
    private String username;

    @Value("${app.email.password}")
    private String password;

    @Value("${app.email.from-name:发件人}")
    private String fromName;

    @Value("${app.email.smtp-ssl:false}")
    private boolean smtpSsl;

    @Value("${app.email.smtp-starttls:false}")
    private boolean smtpStarttls;

    private void doSend(String toEmail, String subject, String content) throws Exception {
        final Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.from", username);
        props.put("mail.user", username);
        props.put("mail.password", password);
        props.put("mail.smtp.connectiontimeout", 10000);
        props.put("mail.smtp.timeout", 10000);

        if (smtpStarttls) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        if (smtpSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.ssl.socketFactory.fallback", "false");
            props.put("mail.smtp.ssl.socketFactory.port", String.valueOf(smtpPort));
        }

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        Session mailSession = Session.getInstance(props, authenticator);

        String messageIDValue = genMessageID(username);
        MimeMessage message = new MimeMessage(mailSession) {
            @Override
            protected void updateMessageID() throws MessagingException {
                setHeader("Message-ID", messageIDValue);
            }
        };

        InternetAddress from = new InternetAddress(username, fromName);
        message.setFrom(from);
        message.setRecipients(Message.RecipientType.TO, new Address[]{new InternetAddress(toEmail)});
        message.setReplyTo(new Address[]{new InternetAddress(username)});
        message.setSentDate(new Date());
        message.setSubject(subject);

        MimeMultipart multipart = new MimeMultipart();
        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(content, "text/html;charset=UTF-8");
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        Transport.send(message);
    }

    /**
     * 发送验证码邮件
     */
    public void sendVerificationCode(String toEmail, String code) {
        try {
            String content = "您的验证码是：" + code + "<br><br>验证码有效期为 10 分钟。<br>请勿将验证码告诉他人。";
            doSend(toEmail, "AI 图片生成平台 - 邮箱验证码", content);
            log.info("验证码邮件已发送至: {}", toEmail);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送通知邮件
     */
    public void sendNotification(String toEmail, String subject, String content) {
        try {
            doSend(toEmail, subject, content);
            log.info("通知邮件已发送至: {}", toEmail);
        } catch (Exception e) {
            log.error("发送通知邮件失败: {}", e.getMessage());
        }
    }

    private String genMessageID(String mailFrom) {
        if (!mailFrom.contains("@")) {
            throw new IllegalArgumentException("Invalid email format: " + mailFrom);
        }
        String domain = mailFrom.split("@")[1];
        return "<" + UUID.randomUUID().toString() + "@" + domain + ">";
    }
}