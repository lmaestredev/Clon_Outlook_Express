package services;

import models.Mail;
import models.User;
import models.UserMail;
import persistence.dao.MailDao;
import persistence.dao.UserMailDao;
import utils.MailFolder;

import java.util.List;

public class MailService {

    private final MailDao mailDao;
    private final UserMailDao userMailDao;

    public MailService(MailDao mailDao, UserMailDao userMailDao) {
        this.mailDao = mailDao;
        this.userMailDao = userMailDao;
    }

    public List<UserMail> getInbox(User user) {
        return userMailDao.findByUserAndFolder(user, MailFolder.INBOX);
    }

    public List<Mail> getSent(User user) {
        return mailDao.findBySender(user);
    }
}
