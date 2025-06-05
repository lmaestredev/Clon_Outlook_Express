package persistence.dao;

import models.Mail;
import models.User;

import java.util.List;
import java.util.UUID;

public interface MailDao {
    void save(Mail mail);
    void update(Mail mail);
    void delete(UUID id);
    Mail findById(UUID id);
    List<Mail> findBySender(User sender);
}
