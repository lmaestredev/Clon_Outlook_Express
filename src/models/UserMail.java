package models;

import utils.MailFolder;

public class UserMail {

    private User user;
    private Mail mail;
    private MailFolder folder;
    private boolean isRead;
    private boolean isDeleted;

    public UserMail(User user, Mail mail, MailFolder folder) {
        this.user = user;
        this.mail = mail;
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

    public MailFolder getFolder() {
        return folder;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}
