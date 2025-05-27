package persistence.impl;

import models.Mail;
import models.User;
import models.UserMail;
import persistence.dao.UserDao;
import persistence.dao.UserMailDao;
import utils.MailFolder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserMailDaoImpl implements UserMailDao {

    private final Connection connection;
    private final UserDao userDao;

    public UserMailDaoImpl(Connection connection, UserDao userDao) {
        this.connection = connection;
        this.userDao = userDao;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_mails (
                user_id UUID,
                mail_id UUID,
                folder VARCHAR(20),
                is_read BOOLEAN,
                is_deleted BOOLEAN,
                PRIMARY KEY (user_id, mail_id)
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user_mails table", e);
        }
    }

    @Override
    public void save(UserMail userMail) {
        String sql = """
            INSERT INTO user_mails (user_id, mail_id, folder, is_read, is_deleted)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, userMail.getUser().getId());
            ps.setObject(2, userMail.getMail().getId());
            ps.setString(3, userMail.getFolder().name());
            ps.setBoolean(4, userMail.isRead());
            ps.setBoolean(5, userMail.isDeleted());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving userMail", e);
        }
    }

    @Override
    public List<UserMail> findByUser(User user) {
        return findByUserAndFolder(user, null);
    }

    @Override
    public List<UserMail> findByUserAndFolder(User user, MailFolder folder) {
        List<UserMail> result = new ArrayList<>();
        String sql = """
        SELECT um.*, m.subject, m.message, m.sender_id
        FROM user_mails um
        JOIN mails m ON um.mail_id = m.id
        WHERE um.user_id = ?
        """ + (folder != null ? " AND um.folder = ?" : "");

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            if (folder != null) ps.setString(2, folder.name());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID mailId = UUID.fromString(rs.getString("mail_id"));
                UUID senderId = UUID.fromString(rs.getString("sender_id"));
                Optional<User> sender = userDao.findById(senderId); // ✅ importante

                Mail mail = new Mail(
                        mailId,
                        sender.orElse(null), // puede que no lo encuentre
                        List.of(),  // TO
                        List.of(),  // CC
                        List.of(),  // BCC
                        null,
                        rs.getString("subject"),
                        rs.getString("message")
                );

                UserMail userMail = new UserMail(user, mail, MailFolder.valueOf(rs.getString("folder")));
                if (rs.getBoolean("is_read")) userMail.markAsRead();
                if (rs.getBoolean("is_deleted")) userMail.markAsDeleted();

                result.add(userMail);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading userMails", e);
        }

        return result;
    }

    @Override
    public void markAsRead(User user, Mail mail) {
        updateFlag(user, mail, "is_read", true);
    }

    @Override
    public void markAsDeleted(User user, Mail mail) {
        updateFlag(user, mail, "is_deleted", true);
    }

    private void updateFlag(User user, Mail mail, String column, boolean value) {
        String sql = "UPDATE user_mails SET " + column + " = ? WHERE user_id = ? AND mail_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, value);
            ps.setObject(2, user.getId());
            ps.setObject(3, mail.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating userMail " + column, e);
        }
    }
}
