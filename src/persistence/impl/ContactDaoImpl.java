package persistence.impl;

import models.Contact;
import models.User;
import persistence.dao.ContactDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContactDaoImpl implements ContactDao {

    private final Connection connection;

    public ContactDaoImpl(Connection connection) {
        this.connection = connection;
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() {
        String contactsSql = """
            CREATE TABLE IF NOT EXISTS contacts (
                id UUID PRIMARY KEY,
                name VARCHAR(100),
                last_name VARCHAR(100),
                email VARCHAR(100) UNIQUE
            )
        """;

        String contactBookSql = """
            CREATE TABLE IF NOT EXISTS contact_book (
                user_id UUID,
                contact_id UUID,
                PRIMARY KEY (user_id, contact_id),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(contactsSql);
            stmt.execute(contactBookSql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating contact tables", e);
        }
    }

    @Override
    public void save(Contact contact) {
        String sql = "INSERT INTO contacts (id, name, last_name, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, contact.getId());
            ps.setString(2, contact.getName());
            ps.setString(3, contact.getLastName());
            ps.setString(4, contact.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 23505 || e.getMessage().contains("Unique index")) {
                throw new RuntimeException("⚠️ Ya existe un contacto con ese email: " + contact.getEmail());
            } else {
                throw new RuntimeException("❌ Error al guardar contacto: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void linkToUser(User user, Contact contact) {
        String sql = "INSERT INTO contact_book (user_id, contact_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setObject(2, contact.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 23505) {
                System.out.println("🔗 El contacto ya está vinculado con el usuario.");
            } else {
                throw new RuntimeException("❌ Error vinculando contacto al usuario", e);
            }
        }
    }

    @Override
    public void unlinkFromUser(User user, Contact contact) {
        String sql = "DELETE FROM contact_book WHERE user_id = ? AND contact_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setObject(2, contact.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error desvinculando contacto del usuario", e);
        }
    }

    @Override
    public List<Contact> findByUser(User user) {
        String sql = """
            SELECT c.* FROM contacts c
            JOIN contact_book cb ON c.id = cb.contact_id
            WHERE cb.user_id = ?
        """;

        List<Contact> contacts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                contacts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error al buscar contactos del usuario", e);
        }

        return contacts;
    }

    @Override
    public Optional<Contact> findByEmail(String email) {
        String sql = "SELECT * FROM contacts WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error buscando contacto por email", e);
        }
        return Optional.empty();
    }

    @Override
    public void delete(Contact contact) {
        String sql = "DELETE FROM contacts WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, contact.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error eliminando contacto", e);
        }
    }

    private Contact mapRow(ResultSet rs) throws SQLException {
        return new Contact(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("last_name"),
                rs.getString("email")
        );
    }
}

