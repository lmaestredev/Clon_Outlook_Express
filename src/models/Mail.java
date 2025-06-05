package models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Mail {

    private final UUID id;
    private User sender;
    private List<User> recipients;
    private List<User> cc;
    private List<User> bcc;
    private LocalDateTime date;
    private String subject;
    private String message;

    public Mail(UUID id, User sender, List<User> recipients, List<User> cc, List<User> bcc, LocalDateTime date, String subject, String message) {
        this.id = id;
        this.sender = sender;
        this.recipients = recipients;
        this.cc = cc;
        this.bcc = bcc;
        this.date = date;
        this.subject = subject;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public List<User> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<User> recipients) {
        this.recipients = recipients;
    }

    public List<User> getCc() {
        return cc;
    }

    public void setCc(List<User> cc) {
        this.cc = cc;
    }

    public List<User> getBcc() {
        return bcc;
    }

    public void setBcc(List<User> bcc) {
        this.bcc = bcc;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
