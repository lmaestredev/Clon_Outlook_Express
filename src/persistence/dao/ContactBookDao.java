package persistence.dao;

import models.User;

import java.util.List;

public interface ContactBookDao {
    void save(User user, User contact);
    void delete(User user, User contact);
    List<User> findByUser(User user);
    boolean exists(User user, User contact);
} 