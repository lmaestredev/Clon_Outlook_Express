package persistence.dao;

import models.Contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactDao {

    void save(Contact contact);
    void linkToUser(UUID userId, UUID contactId);
    List<Contact> findByUser(UUID userId);
    Optional<Contact> findByEmail(String email);
}
