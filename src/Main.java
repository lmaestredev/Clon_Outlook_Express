
import models.Mail;
import models.User;
import models.UserMail;
import persistence.Database;
import persistence.dao.MailDao;
import persistence.dao.UserDao;
import persistence.dao.UserMailDao;
import persistence.impl.MailDaoImpl;
import persistence.impl.UserDaoImpl;
import persistence.impl.UserMailDaoImpl;
import utils.MailFolder;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        try (Connection connection = Database.getConnection()) {
            UserDao userDao = new UserDaoImpl(connection);
            MailDao mailDao = new MailDaoImpl(connection);
            UserMailDao userMailDao = new UserMailDaoImpl(connection, userDao);

            User currentUser = new User(UUID.randomUUID(), "Luis", "Maestre", "lmaestre@palermo.edu");
            userDao.save(currentUser);

            List<User> otherUsers = List.of(
                    new User(UUID.randomUUID(), "Ana", "Gomez", "ana@palermo.edu"),
                    new User(UUID.randomUUID(), "Bruno", "Lopez", "bruno@palermo.edu"),
                    new User(UUID.randomUUID(), "Carla", "Diaz", "carla@palermo.edu"),
                    new User(UUID.randomUUID(), "David", "Fernandez", "david@palermo.edu"),
                    new User(UUID.randomUUID(), "Eva", "Martinez", "eva@palermo.edu"),
                    new User(UUID.randomUUID(), "Franco", "Silva", "franco@palermo.edu"),
                    new User(UUID.randomUUID(), "Gabriela", "Vega", "gabriela@palermo.edu"),
                    new User(UUID.randomUUID(), "Hernan", "Rojas", "hernan@palermo.edu"),
                    new User(UUID.randomUUID(), "Irene", "Castro", "irene@palermo.edu")
            );

            for (User u : otherUsers) {
                userDao.save(u);
            }

            for (int i = 0; i < 5; i++) {
                User sender = otherUsers.get(i);
                Mail mail = new Mail(
                        UUID.randomUUID(),
                        sender,
                        List.of(currentUser),
                        List.of(),
                        List.of(),
                        LocalDateTime.now().minusDays(5 - i),
                        "Mensaje #" + (i + 1) + " para Luis",
                        "Hola Luis, este es el mensaje " + (i + 1)
                );
                mailDao.save(mail);
                userMailDao.save(new UserMail(currentUser, mail, MailFolder.INBOX));
            }

            for (int i = 5; i < 9; i++) {
                User recipient = otherUsers.get(i);
                Mail mail = new Mail(
                        UUID.randomUUID(),
                        currentUser,
                        List.of(recipient),
                        List.of(),
                        List.of(),
                        LocalDateTime.now().minusDays(i),
                        "Correo enviado a " + recipient.getName(),
                        "Hola " + recipient.getName() + ", te escribo este mensaje desde mi cliente de correo."
                );
                mailDao.save(mail);
                userMailDao.save(new UserMail(currentUser, mail, MailFolder.SENT));
            }

            System.out.println("✅ Base de datos cargada con usuarios y correos.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}