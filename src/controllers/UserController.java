package controllers;

import models.User;
import persistence.dao.UserDao;

import java.util.List;
import java.util.Optional;

public class UserController {
    private final UserDao userDao;

    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    public List<User> findAllExcept(User user) {
        return userDao.findAll().stream()
                .filter(u -> !u.getEmail().equals(user.getEmail()))
                .toList();
    }

    public void save(User user) {
        userDao.save(user);
    }
} 