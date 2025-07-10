package persistence.impl;


import models.User;
import models.UserRole;
import persistence.dao.UserDao;

import java.sql.*;
import java.util.*;

public class UserDaoImpl implements UserDao {

    private final Connection connection;

    public UserDaoImpl(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY,
                name VARCHAR(100),
                last_name VARCHAR(100),
                email VARCHAR(100) UNIQUE,
                role VARCHAR(20) DEFAULT 'USER'
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            
            // Agregar columna role si no existe (para tablas existentes)
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER'");
            } catch (SQLException e) {
                // La columna ya existe, ignorar el error
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la tabla de usuarios", e);
        }
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, name, last_name, email, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.executeUpdate();
        }  catch (SQLException e) {
            if (e.getErrorCode() == 23505 || e.getMessage().contains("Unique index")) {
                throw new RuntimeException("⚠️ No se pudo guardar el usuario: ya existe un email igual: " + user.getEmail());
            } else {
                throw new RuntimeException("❌ Error inesperado al guardar el usuario: " + e.getMessage(), e);
            }
        }
    }


    @Override
    public Optional<User> findById(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por email", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al recuperar todos los usuarios", e);
        }
        return users;
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el usuario", e);
        }
    }

    @Override
    public void updateRole(UUID id, models.UserRole role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setObject(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el rol del usuario", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String roleStr = rs.getString("role");
        UserRole role = (roleStr != null) ? UserRole.valueOf(roleStr) : UserRole.USER;
        
        return new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("last_name"),
                rs.getString("email"),
                role
        );
    }
}

