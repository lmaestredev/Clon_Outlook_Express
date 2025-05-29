package ui;

import models.User;
import config.DatabaseConfig;
import persistence.dao.MailDao;
import persistence.dao.UserDao;
import persistence.dao.UserMailDao;
import persistence.impl.MailDaoImpl;
import persistence.impl.UserDaoImpl;
import persistence.impl.UserMailDaoImpl;
import services.MailService;

import java.sql.Connection;

public class AppContext {

    public final User currentUser;
    public final MailService mailService;

    public AppContext() {
        try {
            Connection connection = DatabaseConfig.getConnection();

            UserDao userDao = new UserDaoImpl(connection);
            MailDao mailDao = new MailDaoImpl(connection);
            UserMailDao userMailDao = new UserMailDaoImpl(connection, userDao);

            this.currentUser = userDao.findByEmail("lmaestre@palermo.edu")
                    .orElseThrow(() -> new RuntimeException("Usuario lmaestre@palermo.edu no encontrado"));

            this.mailService = new MailService(mailDao, userMailDao);

        } catch (Exception e) {
            throw new RuntimeException("Error inicializando contexto de aplicación", e);
        }
    }
}
