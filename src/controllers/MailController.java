package controllers;

import models.Mail;
import models.User;
import models.UserMail;
import services.InternalMailService;
import utils.MailFolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MailController {
    private final InternalMailService mailService;
    private final User currentUser;

    public MailController(InternalMailService mailService, User currentUser) {
        this.mailService = mailService;
        this.currentUser = currentUser;
    }

    public void sendMail(User from, String to, String cc, String bcc, String subject, String message) {
        if (from == null) {
            throw new IllegalArgumentException("El remitente no puede ser nulo");
        }

        List<User> recipients = parseEmails(to);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un destinatario");
        }

        List<User> ccList = parseEmails(cc);
        List<User> bccList = parseEmails(bcc);

        mailService.sendMail(from, recipients, ccList, bccList, subject, message);
    }

    public List<UserMail> getInbox() {
        return mailService.findByUserAndFolder(currentUser, MailFolder.INBOX);
    }

    public List<Mail> getSent() {
        return mailService.findSentByUser(currentUser);
    }

    public List<UserMail> getDrafts() {
        return mailService.findByUserAndFolder(currentUser, MailFolder.DRAFTS);
    }

    public void markAsRead(User user, Mail mail) {
        mailService.markAsRead(user, mail);
    }

    public void markAsDeleted(Mail mail) {
        mailService.markAsDeleted(currentUser, mail);
    }

    public Mail createDraft(User user, String to, String cc, String bcc, String subject, String message) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        List<User> recipients = parseEmails(to);
        List<User> ccList = parseEmails(cc);
        List<User> bccList = parseEmails(bcc);

        return mailService.createDraft(user, recipients, ccList, bccList, subject, message);
    }

    public void updateDraft(Mail draft, String to, String cc, String bcc, String subject, String message) {
        if (draft == null) {
            throw new IllegalArgumentException("El borrador no puede ser nulo");
        }

        List<User> recipients = parseEmails(to);
        List<User> ccList = parseEmails(cc);
        List<User> bccList = parseEmails(bcc);

        mailService.updateDraft(draft, recipients, ccList, bccList, subject, message);
    }

    public void deleteDraft(Mail draft) {
        mailService.deleteDraft(currentUser, draft);
    }

    public List<UserMail> findByUserAndFolder(User user, MailFolder folder) {
        return mailService.findByUserAndFolder(user, folder);
    }

    private List<User> parseEmails(String emails) {
        if (emails == null || emails.isEmpty()) {
            return new ArrayList<>();
        }
        return mailService.findUsersByEmails(emails.split(","));
    }
} 