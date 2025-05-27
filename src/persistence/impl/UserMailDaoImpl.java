package persistence.impl;

import models.Mail;
import models.User;
import models.UserMail;
import persistence.dao.UserMailDao;
import utils.MailFolder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserMailDaoImpl implements UserMailDao {

    private final Connection connection;

    public UserMailDaoImpl(Connection connection) {
        this.connection = connection;
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
        String sql = "SELECT * FROM user_mails WHERE user_id = ?";
        if (folder != null) sql += " AND folder = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            if (folder != null) ps.setString(2, folder.name());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Por ahora devolvemos solo los datos de la relación, no reconstruimos Mail completo
                UserMail um = new UserMail(user, new Mail(rs.getObject("mail_id", java.util.UUID.class), null, null, null, null, null, "", ""),
                        MailFolder.valueOf(rs.getString("folder")));
                if (rs.getBoolean("is_read")) um.markAsRead();
                if (rs.getBoolean("is_deleted")) um.markAsDeleted();
                result.add(um);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user mails", e);
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
