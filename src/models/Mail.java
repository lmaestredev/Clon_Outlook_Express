package models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Mail {

    private UUID id;
    private User sender;
    private List<User> to;
    private List<User> cc;
    private List<User> bcc;
    private LocalDateTime date;
    private String subject;
    private String message;

    public Mail(UUID id, User sender, List<User> to, List<User> cc, List<User> bcc, LocalDateTime date, String subject, String message) {
        this.id = id;
        this.sender = sender;
        this.to = to;
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

    public List<User> getTo() {
        return to;
    }

    public List<User> getCc() {
        return cc;
    }

    public List<User> getBcc() {
        return bcc;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setTo(List<User> to) {
        this.to = to;
    }

    public void setCc(List<User> cc) {
        this.cc = cc;
    }

    public void setBcc(List<User> bcc) {
        this.bcc = bcc;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
