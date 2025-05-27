package persistence.dao;

import models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    void save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(UUID id);
}
