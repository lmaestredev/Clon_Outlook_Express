package controllers;

import models.Mail;
import models.User;
import models.UserMail;
import services.InternalMailService;

import java.util.List;

public class MailController {
    private final InternalMailService mailService;
    private final User currentUser;

    public MailController(InternalMailService mailService, User currentUser) {
        this.mailService = mailService;
        this.currentUser = currentUser;
    }

    public void sendMail(List<User> recipients, String subject, String message) {
        mailService.sendMail(currentUser, recipients, subject, message);
    }

    public List<UserMail> getInbox() {
        return mailService.getInbox(currentUser);
    }

    public List<Mail> getSent() {
        return mailService.getSent(currentUser);
    }

    public List<UserMail> getDrafts() {
        return mailService.getDrafts(currentUser);
    }

    public void markAsRead(Mail mail) {
        mailService.markAsRead(currentUser, mail);
    }

    public void moveToTrash(Mail mail) {
        mailService.moveToTrash(currentUser, mail);
    }
} 