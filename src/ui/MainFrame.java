package ui;

import controllers.MailController;
import controllers.UserController;
import models.Mail;
import models.User;
import models.UserMail;
import ui.dialogs.ComposeMailDialog;

import javax.swing.*;

import java.awt.*;

public class MainFrame extends JFrame {

    private final DefaultListModel<Mail> mailListModel = new DefaultListModel<>();
    private final JList<Mail> mailList = new JList<>(mailListModel);
    private final JTextArea messageView = new JTextArea();

    private final User currentUser;
    private final MailController mailController;
    private final UserController userController;

    public MainFrame(User currentUser, MailController mailController, UserController userController) {
        super("Cliente de Correo");
        this.currentUser = currentUser;
        this.mailController = mailController;
        this.userController = userController;

        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Panel izquierdo - carpetas
        JPanel foldersPanel = new JPanel();
        foldersPanel.setLayout(new BoxLayout(foldersPanel, BoxLayout.Y_AXIS));
        foldersPanel.setBorder(BorderFactory.createTitledBorder("Carpetas"));

        JButton inboxButton = new JButton("📥 INBOX");
        inboxButton.addActionListener(e -> loadInbox());
        foldersPanel.add(inboxButton);

        JButton sentButton = new JButton("📤 SENT");
        sentButton.addActionListener(e -> loadSent());
        foldersPanel.add(sentButton);

        JButton draftsButton = new JButton("📝 DRAFTS");
        draftsButton.addActionListener(e -> loadDrafts());
        foldersPanel.add(draftsButton);

        JButton composeButton = new JButton("✉️ Redactar");
        composeButton.addActionListener(e -> {
            var allUsers = userController.findAllExcept(currentUser);
            new ComposeMailDialog(this, mailController, currentUser, allUsers).setVisible(true);
        });
        foldersPanel.add(composeButton);

        add(foldersPanel, BorderLayout.WEST);

        // Lista central de mails
        mailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mailList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String subject = value.getSubject();
            String senderEmail = value.getSender() != null ? value.getSender().getEmail() : "Unknown Sender";
            return new JLabel("Asunto: " + subject + "  |  De: " + senderEmail);
        });

        mailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Mail selectedMail = mailList.getSelectedValue();
                if (selectedMail != null) {
                    messageView.setText(selectedMail.getMessage());
                }
            }
        });

        JScrollPane mailScrollPane = new JScrollPane(mailList);
        mailScrollPane.setBorder(BorderFactory.createTitledBorder("Mensajes"));
        add(mailScrollPane, BorderLayout.CENTER);

        // Panel inferior de lectura
        messageView.setEditable(false);
        JScrollPane messageScroll = new JScrollPane(messageView);
        messageScroll.setBorder(BorderFactory.createTitledBorder("Lectura"));
        messageScroll.setPreferredSize(new Dimension(1000, 150));
        add(messageScroll, BorderLayout.SOUTH);
    }

    private void loadInbox() {
        mailListModel.clear();
        var userMails = mailController.getInbox(currentUser);
        for (UserMail userMail : userMails) {
            mailListModel.addElement(userMail.getMail());
        }
    }

    private void loadSent() {
        mailListModel.clear();
        var sentMails = mailController.getSent(currentUser);
        for (Mail mail : sentMails) {
            mailListModel.addElement(mail);
        }
    }

    private void loadDrafts() {
        mailListModel.clear();
        var userMails = mailController.getDrafts(currentUser);
        for (UserMail userMail : userMails) {
            mailListModel.addElement(userMail.getMail());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                var connection = config.DatabaseConfig.getConnection();
                
                var userDao = new persistence.impl.UserDaoImpl(connection);
                var mailDao = new persistence.impl.MailDaoImpl(connection);
                var userMailDao = new persistence.impl.UserMailDaoImpl(connection, userDao);
                
                var mailSenderService = new services.InternalMailSenderService(mailDao, userMailDao, new config.MailServerConfig("localhost", 25, "", "", false, false));
                
                var userController = new controllers.UserController(userDao);
                var mailController = new controllers.MailController(mailDao, userMailDao, mailSenderService);
                
                var currentUser = userController.findByEmail("lmaestre@palermo.edu")
                        .orElseThrow(() -> new RuntimeException("Usuario lmaestre@palermo.edu no encontrado"));
                
                new MainFrame(currentUser, mailController, userController).setVisible(true);
            } catch (Exception e) {
                throw new RuntimeException("Error inicializando la aplicación", e);
            }
        });
    }
}

