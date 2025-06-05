package models;

import utils.MailFolder;

import java.util.UUID;

public class UserMail {

    private final User user;
    private final Mail mail;
    private final UUID contactId;
    private final MailFolder folder;
    private boolean isRead;
    private boolean isDeleted;

    public UserMail(User user, Mail mail, MailFolder folder) {
        this.user = user;
        this.mail = mail;
        this.contactId = null;
        this.folder = folder;
        this.isRead = false;
        this.isDeleted = false;
    }

    public UserMail(User user, Mail mail, boolean isRead) {
        this.user = user;
        this.mail = mail;
        this.contactId = null;
        this.folder = null;
        this.isRead = isRead;
        this.isDeleted = false;
    }

    public UserMail(User user, UUID contactId, MailFolder folder) {
        this.user = user;
        this.mail = null;
        this.contactId = contactId;
        this.folder = folder;
        this.isRead = false;
        this.isDeleted = false;
    }

    public User getUser() {
        return user;
    }

    public Mail getMail() {
        return mail;
    }

    public UUID getContactId() {
        return contactId;
    }

    public MailFolder getFolder() {
        return folder;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}
