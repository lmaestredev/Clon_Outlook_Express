package persistence.impl;

import models.Mail;
import models.User;
import persistence.dao.MailDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MailDaoImpl implements MailDao {

    private final Connection connection;

    public MailDaoImpl(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS mails (
                id UUID PRIMARY KEY,
                sender_id UUID,
                date TIMESTAMP,
                subject VARCHAR(255),
                message TEXT
            );
            CREATE TABLE IF NOT EXISTS mail_recipients (
                mail_id UUID,
                user_id UUID,
                type VARCHAR(10), -- TO, CC, BCC
                FOREIGN KEY (mail_id) REFERENCES mails(id)
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating mails tables", e);
        }
    }

    @Override
    public void save(Mail mail) {
        try {
            connection.setAutoCommit(false);

            String insertMail = "INSERT INTO mails (id, sender_id, date, subject, message) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertMail)) {
                ps.setObject(1, mail.getId());
                ps.setObject(2, mail.getSender().getId());
                ps.setTimestamp(3, Timestamp.valueOf(mail.getDate()));
                ps.setString(4, mail.getSubject());
                ps.setString(5, mail.getMessage());
                ps.executeUpdate();
            }

            insertRecipients(mail.getId(), mail.getTo(), "TO");
            insertRecipients(mail.getId(), mail.getCc(), "CC");
            insertRecipients(mail.getId(), mail.getBcc(), "BCC");

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Error saving mail", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private void insertRecipients(UUID mailId, List<User> users, String type) throws SQLException {
        String sql = "INSERT INTO mail_recipients (mail_id, user_id, type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (User user : users) {
                ps.setObject(1, mailId);
                ps.setObject(2, user.getId());
                ps.setString(3, type);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<Mail> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<Mail> findAll() {
        return new ArrayList<>();
    }

    @Override
    public void deleteById(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM mails WHERE id = ?")) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting mail", e);
        }
    }
}
