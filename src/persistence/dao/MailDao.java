package persistence.dao;

import models.Mail;
import models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailDao {
    void save(Mail mail);
    List<Mail> findBySender(User sender);
    Optional<Mail> findById(UUID id);
    List<Mail> findAll();
    void deleteById(UUID id);
}
