package controllers;

import models.User;
import persistence.dao.ContactBookDao;
import persistence.dao.UserDao;

import java.util.List;
import java.util.Optional;

public class ContactsController {
    private final UserDao userDao;
    private final ContactBookDao contactBookDao;
    private final User currentUser;

    public ContactsController(UserDao userDao, ContactBookDao contactBookDao, User currentUser) {
        this.userDao = userDao;
        this.contactBookDao = contactBookDao;
        this.currentUser = currentUser;
    }

    public List<User> getAllContacts() {
        return contactBookDao.findByUser(currentUser);
    }

    public void addContact(String email) {
        Optional<User> contactOpt = userDao.findByEmail(email);
        if (contactOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe un usuario con ese email");
        }

        User contact = contactOpt.get();
        if (contact.equals(currentUser)) {
            throw new IllegalArgumentException("No puedes agregarte a ti mismo como contacto");
        }

        if (contactBookDao.exists(currentUser, contact)) {
            throw new IllegalArgumentException("Este usuario ya está en tus contactos");
        }

        contactBookDao.save(currentUser, contact);
    }

    public void removeContact(User contact) {
        contactBookDao.delete(currentUser, contact);
    }
} 