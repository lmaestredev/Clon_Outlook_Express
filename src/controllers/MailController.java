package controllers;

import models.Mail;
import models.User;
import models.UserMail;
import persistence.dao.MailDao;
import persistence.dao.UserMailDao;
import services.InternalMailSenderService;
import utils.MailFolder;

import java.util.List;

public class MailController {
    private final MailDao mailDao;
    private final UserMailDao userMailDao;
    private final InternalMailSenderService mailSenderService;

    public MailController(MailDao mailDao, UserMailDao userMailDao, InternalMailSenderService mailSenderService) {
        this.mailDao = mailDao;
        this.userMailDao = userMailDao;
        this.mailSenderService = mailSenderService;
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

    public void sendMail(User sender, List<User> recipients, String subject, String message) {
        mailSenderService.sendInternal(sender, recipients, subject, message);
    }
} 