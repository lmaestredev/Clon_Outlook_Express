package persistence.impl;

import models.User;
import persistence.dao.ContactBookDao;
import persistence.dao.UserDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContactBookDaoImpl implements ContactBookDao {
    private final Connection connection;
    private final UserDao userDao;

    public ContactBookDaoImpl(Connection connection, UserDao userDao) {
        this.connection = connection;
        this.userDao = userDao;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS contact_book (
                user_id UUID NOT NULL,
                contact_id UUID NOT NULL,
                PRIMARY KEY (user_id, contact_id),
                CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                CONSTRAINT fk_contact FOREIGN KEY (contact_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating contact_book table: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(User user, User contact) {
        if (user == null || contact == null) {
            throw new IllegalArgumentException("El usuario y el contacto no pueden ser nulos");
        }
        if (user.getId() == null || contact.getId() == null) {
            throw new IllegalArgumentException("El usuario y el contacto deben tener IDs válidos");
        }
        
        String sql = "INSERT INTO contact_book (user_id, contact_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setObject(2, contact.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Unique violation
                System.out.println(e.getMessage());
                throw new RuntimeException("El contacto ya existe", e);
            } else if (e.getSQLState().equals("23503")) { // Foreign key violation
                System.out.println(e.getMessage());
                throw new RuntimeException("ID de usuario o contacto inválido", e);
            }
            System.out.println(e.getMessage());
            throw new RuntimeException("Error al guardar el contacto: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(User user, User contact) {
        if (user == null || contact == null) {
            throw new IllegalArgumentException("User and contact cannot be null");
        }
        
        String sql = "DELETE FROM contact_book WHERE user_id = ? AND contact_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setObject(2, contact.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting contact: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        List<User> contacts = new ArrayList<>();
        String sql = "SELECT contact_id FROM contact_book WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID contactId = UUID.fromString(rs.getString("contact_id"));
                Optional<User> contact = userDao.findById(contactId);
                contact.ifPresent(contacts::add);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding contacts: " + e.getMessage(), e);
        }
        return contacts;
    }

    @Override
    public boolean exists(User user, User contact) {
        if (user == null || contact == null) {
            throw new IllegalArgumentException("User and contact cannot be null");
        }
        
        String sql = "SELECT COUNT(*) FROM contact_book WHERE user_id = ? AND contact_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setObject(2, contact.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if contact exists: " + e.getMessage(), e);
        }
        return false;
    }
} 