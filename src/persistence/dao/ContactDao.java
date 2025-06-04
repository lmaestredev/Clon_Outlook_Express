package persistence.dao;

import models.Contact;
import models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactDao {

    void save(Contact contact);
    void linkToUser(User user, Contact contact);
    List<Contact> findByUser(User user);
    Optional<Contact> findByEmail(String email);
    void unlinkFromUser(User user, Contact contact);
    void delete(Contact contact);
}
