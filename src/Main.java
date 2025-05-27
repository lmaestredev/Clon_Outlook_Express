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
            UserMailDao userMailDao = new UserMailDaoImpl(connection);

            User sender = new User(UUID.randomUUID(), "Luis", "Maestre", "luis@example.com");
            User receiver = new User(UUID.randomUUID(), "Ana", "Gómez", "ana@example.com");
            userDao.save(sender);
            userDao.save(receiver);

            Mail mail = new Mail(
                    UUID.randomUUID(),
                    sender,
                    List.of(receiver),
                    List.of(),
                    List.of(),
                    LocalDateTime.now(),
                    "¡Hola!",
                    "Este es un mensaje de prueba."
            );
            mailDao.save(mail);

            UserMail userMail = new UserMail(receiver, mail, MailFolder.INBOX);
            userMailDao.save(userMail);

            userMailDao.markAsRead(receiver, mail);

            List<UserMail> inbox = userMailDao.findByUserAndFolder(receiver, MailFolder.INBOX);
            for (UserMail um : inbox) {
                System.out.println("📬 Mail ID: " + um.getMail().getId());
                System.out.println("🗂️  Carpeta: " + um.getFolder());
                System.out.println("👁️  Leído: " + um.isRead());
                System.out.println("🗑️  Eliminado: " + um.isDeleted());
                System.out.println("--------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
