package services;

import models.Mail;
import models.User;
import models.UserMail;
import persistence.dao.MailDao;
import persistence.dao.UserDao;
import persistence.dao.UserMailDao;
import utils.MailFolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class InternalMailService {
    private final MailDao mailDao;
    private final UserMailDao userMailDao;
    private final UserDao userDao;

    public InternalMailService(MailDao mailDao, UserMailDao userMailDao, UserDao userDao) {
        this.mailDao = mailDao;
        this.userMailDao = userMailDao;
        this.userDao = userDao;
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
            throw new IllegalArgumentException("User cannot be null");
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
            throw new IllegalArgumentException("Draft cannot be null");
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
            throw new IllegalArgumentException("Sender cannot be null");
        }
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required");
        }

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
} 