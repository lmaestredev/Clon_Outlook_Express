package controllers;

import models.Contact;
import models.User;
import persistence.dao.ContactDao;

import java.util.List;
import java.util.UUID;

public class ContactsController {
    private final ContactDao contactDao;
    private final User currentUser;

    public ContactsController(ContactDao contactDao, User currentUser) {
        this.contactDao = contactDao;
        this.currentUser = currentUser;
    }

    public List<Contact> getAllContacts() {
        return contactDao.findByUser(currentUser);
    }

    public void addContact(String name, String lastName, String email) {
        var existingContact = contactDao.findByEmail(email);
        Contact contact;
        
        if (existingContact.isPresent()) {
            contact = existingContact.get();
        } else {
            contact = new Contact(UUID.randomUUID(), name, lastName, email);
            contactDao.save(contact);
        }
        
        contactDao.linkToUser(currentUser, contact);
    }

    public void removeContact(Contact contact) {
        contactDao.unlinkFromUser(currentUser, contact);
    }
} 