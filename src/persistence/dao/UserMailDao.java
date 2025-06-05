package persistence.dao;

import models.Mail;
import models.User;
import models.UserMail;
import utils.MailFolder;

import java.util.List;

public interface UserMailDao {
    void save(UserMail userMail);
    void delete(User user, Mail mail);
    List<UserMail> findByUser(User user);
    List<UserMail> findByUserAndFolder(User user, MailFolder folder);
    void markAsRead(User user, Mail mail);
    void markAsDeleted(User user, Mail mail);
}
