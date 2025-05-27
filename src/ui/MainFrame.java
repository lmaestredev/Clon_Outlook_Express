package ui;

import models.Mail;
import models.UserMail;

import javax.swing.*;

import java.awt.*;

public class MainFrame extends JFrame {

    private final DefaultListModel<Mail> mailListModel = new DefaultListModel<>();
    private final JList<Mail> mailList = new JList<>(mailListModel);
    private final JTextArea messageView = new JTextArea();

    private final AppContext context;

    public MainFrame(AppContext context) {
        super("Cliente de Correo");
        this.context = context;

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
        var userMails = context.mailService.getInbox(context.currentUser);
        for (UserMail userMail : userMails) {
            mailListModel.addElement(userMail.getMail());
        }
    }

    private void loadSent() {
        mailListModel.clear();
        var sentMails = context.mailService.getSent(context.currentUser);
        for (Mail mail : sentMails) {
            mailListModel.addElement(mail);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppContext context = new AppContext();
            new MainFrame(context).setVisible(true);
        });
    }
}
