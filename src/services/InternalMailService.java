package services;

import config.MailServerConfig;
import models.Mail;
import models.User;
import models.UserMail;
import persistence.dao.MailDao;
import persistence.dao.UserDao;
import persistence.dao.UserMailDao;
import utils.MailFolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InternalMailService {
    private final MailDao mailDao;
    private final UserMailDao userMailDao;
    private final UserDao userDao;
    private MailServerConfig serverConfig;

    public InternalMailService(MailDao mailDao, UserMailDao userMailDao, UserDao userDao) {
        this.mailDao = mailDao;
        this.userMailDao = userMailDao;
        this.userDao = userDao;
        // Configuración por defecto para simulación
        this.serverConfig = new MailServerConfig("smtp.palermo.edu", 587, "", "", true, true);
    }

    public void setServerConfig(MailServerConfig config) {
        this.serverConfig = config;
    }

    public MailServerConfig getServerConfig() {
        return serverConfig;
    }

    public List<User> findUsersByEmails(String[] emails) {
        List<User> users = new ArrayList<>();
        for (String email : emails) {
            userDao.findByEmail(email.trim())
                    .ifPresent(users::add);
        }
        return users;
    }

    public void sendMail(User from, List<User> recipients, String subject, String message) {
        Mail mail = new Mail(
                UUID.randomUUID(),
                from,
                recipients,
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                subject,
                message
        );
        mailDao.save(mail);

        UserMail sentMail = new UserMail(from, mail, MailFolder.SENT);
        userMailDao.save(sentMail);

        for (User recipient : recipients) {
            UserMail receivedMail = new UserMail(recipient, mail, MailFolder.INBOX);
            userMailDao.save(receivedMail);
        }
    }

    public List<UserMail> findByUserAndFolder(User user, MailFolder folder) {
        return userMailDao.findByUserAndFolder(user, folder);
    }

    public List<Mail> findSentByUser(User user) {
        return findByUserAndFolder(user, MailFolder.SENT).stream()
                .map(UserMail::getMail)
                .toList();
    }

    public void markAsRead(User user, Mail mail) {
        userMailDao.markAsRead(user, mail);
    }

    public void markAsDeleted(User user, Mail mail) {
        userMailDao.markAsDeleted(user, mail);
    }

    public Mail createDraft(User user, List<User> recipients, List<User> cc, List<User> bcc, String subject, String message) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        Mail draft = new Mail(
            UUID.randomUUID(),
            user,
            recipients,
            cc,
            bcc,
            LocalDateTime.now(),
            subject,
            message
        );

        mailDao.save(draft);
        
        UserMail userMail = new UserMail(user, draft, MailFolder.DRAFTS);
        userMailDao.save(userMail);
        
        return draft;
    }

    public void updateDraft(Mail draft, List<User> recipients, List<User> cc, List<User> bcc, String subject, String message) {
        if (draft == null) {
            throw new IllegalArgumentException("El borrador no puede ser nulo");
        }

        draft.setRecipients(recipients);
        draft.setCc(cc);
        draft.setBcc(bcc);
        draft.setSubject(subject);
        draft.setMessage(message);
        draft.setDate(LocalDateTime.now());

        mailDao.update(draft);
    }

    public void deleteDraft(User user, Mail draft) {
        if (user == null || draft == null) {
            throw new IllegalArgumentException("El usuario y el borrador no pueden ser nulos");
        }

        userMailDao.delete(user, draft);
        mailDao.delete(draft.getId());
    }

    public void sendMail(User sender, List<User> recipients, List<User> cc, List<User> bcc, String subject, String message) {
        if (sender == null) {
            throw new IllegalArgumentException("El remitente no puede ser nulo");
        }
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un destinatario");
        }

        // Simular envío usando la configuración del servidor
        simulateSendMail(sender, recipients, cc, bcc, subject, message);

        Mail mail = new Mail(
            UUID.randomUUID(),
            sender,
            recipients,
            cc,
            bcc,
            LocalDateTime.now(),
            subject,
            message
        );

        mailDao.save(mail);

        for (User recipient : recipients) {
            userMailDao.save(new UserMail(recipient, mail, MailFolder.INBOX));
        }

        for (User ccUser : cc) {
            userMailDao.save(new UserMail(ccUser, mail, MailFolder.INBOX));
        }

        for (User bccUser : bcc) {
            userMailDao.save(new UserMail(bccUser, mail, MailFolder.INBOX));
        }

        userMailDao.save(new UserMail(sender, mail, MailFolder.SENT));
    }

    /**
     * Simula el envío de correo usando la configuración del servidor
     * En una implementación real, aquí se conectaría al servidor SMTP
     */
    private void simulateSendMail(User sender, List<User> recipients, List<User> cc, List<User> bcc, String subject, String message) {
        System.out.println("=== SIMULACIÓN DE ENVÍO DE CORREO ===");
        System.out.println("Servidor SMTP: " + serverConfig.getHost());
        System.out.println("Puerto: " + serverConfig.getPort());
        System.out.println("Autenticación: " + (serverConfig.isAuth() ? "Sí" : "No"));
        System.out.println("STARTTLS: " + (serverConfig.isStarttls() ? "Sí" : "No"));
        System.out.println("De: " + sender.getEmail() + "@palermo.edu");
        System.out.println("Para: " + recipients.stream().map(u -> u.getEmail() + "@palermo.edu").toList());
        if (!cc.isEmpty()) {
            System.out.println("CC: " + cc.stream().map(u -> u.getEmail() + "@palermo.edu").toList());
        }
        if (!bcc.isEmpty()) {
            System.out.println("BCC: " + bcc.stream().map(u -> u.getEmail() + "@palermo.edu").toList());
        }
        System.out.println("Asunto: " + subject);
        System.out.println("Mensaje: " + message.substring(0, Math.min(message.length(), 100)) + (message.length() > 100 ? "..." : ""));
        System.out.println("=== FIN SIMULACIÓN ===");
    }
} 