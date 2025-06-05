package persistence.impl;

import models.Mail;
import models.User;
import persistence.dao.MailDao;
import persistence.dao.UserDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class MailDaoImpl implements MailDao {
    private final Connection connection;
    private final UserDao userDao;

    public MailDaoImpl(Connection connection, UserDao userDao) {
        this.connection = connection;
        this.userDao = userDao;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS mails (
                id UUID PRIMARY KEY,
                sender_id UUID,
                subject VARCHAR(255),
                message TEXT,
                mail_date TIMESTAMP,
                cc TEXT,
                bcc TEXT,
                FOREIGN KEY (sender_id) REFERENCES users(id)
            );
            
            CREATE TABLE IF NOT EXISTS mail_recipients (
                mail_id UUID,
                recipient_id UUID,
                PRIMARY KEY (mail_id, recipient_id),
                FOREIGN KEY (mail_id) REFERENCES mails(id) ON DELETE CASCADE,
                FOREIGN KEY (recipient_id) REFERENCES users(id)
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear las tablas de correos: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Mail mail) {
        String mailSql = "INSERT INTO mails (id, sender_id, subject, message, mail_date, cc, bcc) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String recipientSql = "INSERT INTO mail_recipients (mail_id, recipient_id) VALUES (?, ?)";

        try (PreparedStatement mailPs = connection.prepareStatement(mailSql);
             PreparedStatement recipientPs = connection.prepareStatement(recipientSql)) {
            
            connection.setAutoCommit(false);
            try {
                mailPs.setObject(1, mail.getId());
                mailPs.setObject(2, mail.getSender().getId());
                mailPs.setString(3, mail.getSubject());
                mailPs.setString(4, mail.getMessage());
                mailPs.setObject(5, mail.getDate() != null ? mail.getDate() : LocalDateTime.now());
                mailPs.setString(6, mail.getCc().stream().map(User::getEmail).collect(Collectors.joining(",")));
                mailPs.setString(7, mail.getBcc().stream().map(User::getEmail).collect(Collectors.joining(",")));
                mailPs.executeUpdate();

                for (User recipient : mail.getRecipients()) {
                    recipientPs.setObject(1, mail.getId());
                    recipientPs.setObject(2, recipient.getId());
                    recipientPs.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error al guardar el correo", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving mail", e);
        }
    }

    @Override
    public void update(Mail mail) {
        String mailSql = "UPDATE mails SET sender_id = ?, subject = ?, message = ?, mail_date = ?, cc = ?, bcc = ? WHERE id = ?";
        String deleteRecipientsSql = "DELETE FROM mail_recipients WHERE mail_id = ?";
        String recipientSql = "INSERT INTO mail_recipients (mail_id, recipient_id) VALUES (?, ?)";

        try (PreparedStatement mailPs = connection.prepareStatement(mailSql);
             PreparedStatement deleteRecipientsPs = connection.prepareStatement(deleteRecipientsSql);
             PreparedStatement recipientPs = connection.prepareStatement(recipientSql)) {
            
            connection.setAutoCommit(false);
            try {
                mailPs.setObject(1, mail.getSender().getId());
                mailPs.setString(2, mail.getSubject());
                mailPs.setString(3, mail.getMessage());
                mailPs.setObject(4, mail.getDate() != null ? mail.getDate() : LocalDateTime.now());
                mailPs.setString(5, mail.getCc().stream().map(User::getEmail).collect(Collectors.joining(",")));
                mailPs.setString(6, mail.getBcc().stream().map(User::getEmail).collect(Collectors.joining(",")));
                mailPs.setObject(7, mail.getId());
                mailPs.executeUpdate();

                deleteRecipientsPs.setObject(1, mail.getId());
                deleteRecipientsPs.executeUpdate();

                for (User recipient : mail.getRecipients()) {
                    recipientPs.setObject(1, mail.getId());
                    recipientPs.setObject(2, recipient.getId());
                    recipientPs.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error al actualizar el correo", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating mail", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM mails WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el correo", e);
        }
    }

    @Override
    public Mail findById(UUID id) {
        String mailSql = "SELECT * FROM mails WHERE id = ?";
        String recipientsSql = "SELECT recipient_id FROM mail_recipients WHERE mail_id = ?";

        try (PreparedStatement mailPs = connection.prepareStatement(mailSql);
             PreparedStatement recipientsPs = connection.prepareStatement(recipientsSql)) {
            
            mailPs.setObject(1, id);
            ResultSet mailRs = mailPs.executeQuery();
            
            if (mailRs.next()) {
                UUID senderId = UUID.fromString(mailRs.getString("sender_id"));
                User sender = userDao.findById(senderId).orElse(null);

                List<User> recipients = new ArrayList<>();
                recipientsPs.setObject(1, id);
                ResultSet recipientsRs = recipientsPs.executeQuery();
                while (recipientsRs.next()) {
                    UUID recipientId = UUID.fromString(recipientsRs.getString("recipient_id"));
                    userDao.findById(recipientId).ifPresent(recipients::add);
                }

                List<User> cc = new ArrayList<>();
                String ccEmails = mailRs.getString("cc");
                if (ccEmails != null && !ccEmails.isEmpty()) {
                    for (String email : ccEmails.split(",")) {
                        userDao.findByEmail(email).ifPresent(cc::add);
                    }
                }

                List<User> bcc = new ArrayList<>();
                String bccEmails = mailRs.getString("bcc");
                if (bccEmails != null && !bccEmails.isEmpty()) {
                    for (String email : bccEmails.split(",")) {
                        userDao.findByEmail(email).ifPresent(bcc::add);
                    }
                }

                return new Mail(
                    id,
                    sender,
                    recipients,
                    cc,
                    bcc,
                    mailRs.getTimestamp("mail_date").toLocalDateTime(),
                    mailRs.getString("subject"),
                    mailRs.getString("message")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el correo por id", e);
        }
    }

    @Override
    public List<Mail> findBySender(User sender) {
        List<Mail> mails = new ArrayList<>();
        String mailSql = "SELECT * FROM mails WHERE sender_id = ?";
        String recipientsSql = "SELECT recipient_id FROM mail_recipients WHERE mail_id = ?";

        try (PreparedStatement mailPs = connection.prepareStatement(mailSql);
             PreparedStatement recipientsPs = connection.prepareStatement(recipientsSql)) {
            
            mailPs.setObject(1, sender.getId());
            ResultSet mailRs = mailPs.executeQuery();
            
            while (mailRs.next()) {
                UUID mailId = UUID.fromString(mailRs.getString("id"));
                List<User> recipients = new ArrayList<>();
                
                recipientsPs.setObject(1, mailId);
                ResultSet recipientsRs = recipientsPs.executeQuery();
                while (recipientsRs.next()) {
                    UUID recipientId = UUID.fromString(recipientsRs.getString("recipient_id"));
                    userDao.findById(recipientId).ifPresent(recipients::add);
                }

                List<User> cc = new ArrayList<>();
                String ccEmails = mailRs.getString("cc");
                if (ccEmails != null && !ccEmails.isEmpty()) {
                    for (String email : ccEmails.split(",")) {
                        userDao.findByEmail(email).ifPresent(cc::add);
                    }
                }

                List<User> bcc = new ArrayList<>();
                String bccEmails = mailRs.getString("bcc");
                if (bccEmails != null && !bccEmails.isEmpty()) {
                    for (String email : bccEmails.split(",")) {
                        userDao.findByEmail(email).ifPresent(bcc::add);
                    }
                }

                mails.add(new Mail(
                    mailId,
                    sender,
                    recipients,
                    cc,
                    bcc,
                    mailRs.getTimestamp("mail_date").toLocalDateTime(),
                    mailRs.getString("subject"),
                    mailRs.getString("message")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar los correos por remitente", e);
        }
        return mails;
    }
}
