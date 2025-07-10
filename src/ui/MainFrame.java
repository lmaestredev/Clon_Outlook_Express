package ui;

import controllers.ContactsController;
import controllers.MailController;
import controllers.UserController;
import models.User;
import models.UserMail;
import persistence.dao.UserDao;
import ui.dialogs.ComposeMailDialog;
import ui.dialogs.ContactsDialog;
import ui.dialogs.EmailHistoryDialog;
import ui.dialogs.ServerConfigDialog;
import ui.dialogs.UserManagementDialog;
import services.EmailHistoryService;
import utils.MailFolder;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class MainFrame extends JFrame {

    private final DefaultListModel<UserMail> mailListModel = new DefaultListModel<>();
    private final JList<UserMail> mailList = new JList<>(mailListModel);
    private final JTextArea messageView = new JTextArea();

    private final User currentUser;
    private final MailController mailController;
    private final UserController userController;
    private final ContactsController contactsController;
    private final UserDao userDao;
    private final services.InternalMailService mailService;
    private final EmailHistoryService emailHistoryService;
    private MailFolder currentFolder;

    public MainFrame(User currentUser, MailController mailController, UserController userController, ContactsController contactsController, UserDao userDao, services.InternalMailService mailService, EmailHistoryService emailHistoryService) {
        super("Cliente de Correo - " + currentUser.getEmail() + " (" + currentUser.getRole() + ")");
        this.currentUser = currentUser;
        this.mailController = mailController;
        this.userController = userController;
        this.contactsController = contactsController;
        this.userDao = userDao;
        this.mailService = mailService;
        this.emailHistoryService = emailHistoryService;

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
        inboxButton.addActionListener(e -> {
            currentFolder = MailFolder.INBOX;
            updateMailList();
        });
        foldersPanel.add(inboxButton);

        JButton sentButton = new JButton("📤 SENT");
        sentButton.addActionListener(e -> {
            currentFolder = MailFolder.SENT;
            updateMailList();
        });
        foldersPanel.add(sentButton);

        JButton draftsButton = new JButton("📝 DRAFTS");
        draftsButton.addActionListener(e -> {
            currentFolder = MailFolder.DRAFTS;
            updateMailList();
        });
        foldersPanel.add(draftsButton);

        JButton contactsButton = new JButton("👥 Contactos");
        contactsButton.addActionListener(e -> {
            new ContactsDialog(this, contactsController, currentUser, emailHistoryService).setVisible(true);
        });
        foldersPanel.add(contactsButton);

        JButton composeButton = new JButton("✉️ Redactar");
        composeButton.addActionListener(e -> {
            ComposeMailDialog composeDialog = new ComposeMailDialog(this, mailController, currentUser, userDao.findAll(), emailHistoryService);
            composeDialog.setVisible(true);
        });
        foldersPanel.add(composeButton);

        // Botones solo para administradores
        if (currentUser.getRole() == models.UserRole.ADMIN) {
            JButton configButton = new JButton("⚙️ Configuración");
            configButton.addActionListener(e -> {
                ServerConfigDialog configDialog = new ServerConfigDialog(this);
                // Cargar configuración actual
                configDialog.setServerConfig(mailService.getServerConfig());
                configDialog.setVisible(true);
                // Guardar nueva configuración si se modificó
                if (configDialog.getServerConfig() != null) {
                    mailService.setServerConfig(configDialog.getServerConfig());
                    JOptionPane.showMessageDialog(this, "Configuración del servidor actualizada", "Configuración", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            foldersPanel.add(configButton);

            JButton historyButton = new JButton("📧 Historial Emails");
            historyButton.addActionListener(e -> {
                EmailHistoryDialog historyDialog = new EmailHistoryDialog(this, emailHistoryService, currentUser);
                historyDialog.setVisible(true);
            });
            foldersPanel.add(historyButton);

            JButton userManagementButton = new JButton("👥 Gestión Usuarios");
            userManagementButton.addActionListener(e -> {
                UserManagementDialog userManagementDialog = new UserManagementDialog(this, userDao);
                userManagementDialog.setVisible(true);
            });
            foldersPanel.add(userManagementButton);
        }

        add(foldersPanel, BorderLayout.WEST);

        // Lista central de mails
        mailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mailList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String subject = value.getMail().getSubject();
            String info;
            if (currentFolder == MailFolder.SENT) {
                // Mostrar destinatarios principales
                List<User> recipients = value.getMail().getRecipients();
                String to = recipients.isEmpty() ? "Sin destinatario" :
                    recipients.stream().map(User::getEmail).reduce((a, b) -> a + ", " + b).orElse("");
                info = "Para: " + to;
            } else {
                // Mostrar remitente
                String senderEmail = value.getMail().getSender() != null ? value.getMail().getSender().getEmail() : "Desconocido";
                info = "De: " + senderEmail;
            }
            return new JLabel("Asunto: " + subject + "  |  " + info);
        });

        mailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                UserMail selectedMail = mailList.getSelectedValue();
                if (selectedMail != null) {
                    if (currentFolder == MailFolder.DRAFTS) {
                        ComposeMailDialog composeDialog = new ComposeMailDialog(this, mailController, currentUser, userDao.findAll(), emailHistoryService);
                        composeDialog.loadDraft(selectedMail.getMail());
                        composeDialog.setVisible(true);
                    } else {
                        messageView.setText(selectedMail.getMail().getMessage());
                        if (!selectedMail.isRead()) {
                            mailController.markAsRead(currentUser, selectedMail.getMail());
                        }
                    }
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

        // Cargar bandeja de entrada por defecto
        currentFolder = MailFolder.INBOX;
        updateMailList();
    }

    private void updateMailList() {
        List<UserMail> mails = mailController.findByUserAndFolder(currentUser, currentFolder);
        mailListModel.clear();
        for (UserMail mail : mails) {
            mailListModel.addElement(mail);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Connection connection = config.DatabaseConfig.getConnection();
                
                var userDao = new persistence.impl.UserDaoImpl(connection);
                var mailDao = new persistence.impl.MailDaoImpl(connection, userDao);
                var userMailDao = new persistence.impl.UserMailDaoImpl(connection, userDao);
                var contactBookDao = new persistence.impl.ContactBookDaoImpl(connection, userDao);
                
                var internalMailService = new services.InternalMailService(mailDao, userMailDao, userDao);
                
                var userController = new controllers.UserController(userDao);
                
                // Mostrar login
                ui.dialogs.LoginDialog loginDialog = new ui.dialogs.LoginDialog(null, userController);
                loginDialog.setVisible(true);
                models.User currentUser = loginDialog.getLoggedUser();
                if (currentUser == null) {
                    System.exit(0);
                }
                
                var mailController = new controllers.MailController(internalMailService, currentUser);
                var contactsController = new controllers.ContactsController(userDao, contactBookDao, currentUser);
                var emailHistoryService = new services.EmailHistoryService(userDao);
                
                // Probar el autocompletado
                emailHistoryService.testAutocomplete();
                
                new MainFrame(currentUser, mailController, userController, contactsController, userDao, internalMailService, emailHistoryService).setVisible(true);
            } catch (Exception e) {
                throw new RuntimeException("Error inicializando la aplicación", e);
            }
        });
    }
}

