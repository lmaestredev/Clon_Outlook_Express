package persistence.dao;

import models.Mail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailDao {
    void save(Mail mail);
    Optional<Mail> findById(UUID id);
    List<Mail> findAll();
    void deleteById(UUID id);
}
