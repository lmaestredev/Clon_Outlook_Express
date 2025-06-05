package services;

import models.Mail;
import models.User;
import models.UserMail;
import persistence.dao.MailDao;
import persistence.dao.UserMailDao;
import utils.MailFolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InternalMailService {
    private final MailDao mailDao;
    private final UserMailDao userMailDao;

    public InternalMailService(MailDao mailDao, UserMailDao userMailDao) {
        this.mailDao = mailDao;
        this.userMailDao = userMailDao;
    }

    public void sendMail(User sender, List<User> recipients, String subject, String message) {
        // Crear y guardar el correo
        Mail mail = new Mail(
            UUID.randomUUID(),
            sender,
            recipients,
            List.of(), // CC
            List.of(), // BCC
            LocalDateTime.now(),
            subject,
            message
        );
        mailDao.save(mail);

        // Guardar en la carpeta de enviados del remitente
        userMailDao.save(new UserMail(sender, mail, MailFolder.SENT));

        // Guardar en la bandeja de entrada de cada destinatario
        for (User recipient : recipients) {
            userMailDao.save(new UserMail(recipient, mail, MailFolder.INBOX));
        }
    }

    public List<UserMail> getInbox(User user) {
        return userMailDao.findByUserAndFolder(user, MailFolder.INBOX);
    }

    public List<Mail> getSent(User user) {
        return mailDao.findBySender(user);
    }

    public List<UserMail> getDrafts(User user) {
        return userMailDao.findByUserAndFolder(user, MailFolder.DRAFTS);
    }

    public void markAsRead(User user, Mail mail) {
        userMailDao.markAsRead(user, mail);
    }

    public void moveToTrash(User user, Mail mail) {
        userMailDao.markAsDeleted(user, mail);
    }
} 