package services;

import config.MailServerConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import models.Mail;
import models.User;
import models.UserMail;
import persistence.dao.MailDao;
import persistence.dao.UserMailDao;
import utils.MailFolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class InternalMailSenderService {

    private final MailDao mailDao;
    private final UserMailDao userMailDao;
    private final MailServerConfig config;

    public InternalMailSenderService(MailDao mailDao, UserMailDao userMailDao, MailServerConfig config) {
        this.mailDao = mailDao;
        this.userMailDao = userMailDao;
        this.config = config;
    }

    public void sendInternal(User sender, List<User> to, String subject, String messageText) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", config.getHost());
            props.put("mail.smtp.port", String.valueOf(config.getPort()));
            props.put("mail.smtp.auth", String.valueOf(config.isAuth()));
            props.put("mail.smtp.starttls.enable", String.valueOf(config.isStarttls()));

            Session session;
            if (config.isAuth()) {
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.getUsername(), config.getPassword());
                    }
                });
            } else {
                session = Session.getInstance(props);
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender.getEmail()));
            for (User recipient : to) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getEmail()));
            }
            message.setSubject(subject);
            message.setText(messageText);

            // Si más adelante integrás con servidor real, descomentar:
            // Transport.send(message);

            // Persistencia interna simulada
            Mail mail = new Mail(UUID.randomUUID(), sender, to, List.of(), List.of(), LocalDateTime.now(), subject, messageText);
            mailDao.save(mail);

            userMailDao.save(new UserMail(sender, mail, MailFolder.SENT));
            for (User recipient : to) {
                userMailDao.save(new UserMail(recipient, mail, MailFolder.INBOX));
            }

            System.out.println("✅ Mail interno enviado y registrado correctamente");

        } catch (MessagingException e) {
            throw new RuntimeException("❌ Error al construir o enviar el mensaje", e);
        }
    }
}
