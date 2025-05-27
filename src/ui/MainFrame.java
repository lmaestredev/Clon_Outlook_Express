package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Cliente de Correo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel foldersPanel = new JPanel();
        foldersPanel.setLayout(new BoxLayout(foldersPanel, BoxLayout.Y_AXIS));
        foldersPanel.setBorder(BorderFactory.createTitledBorder("Carpetas"));
        foldersPanel.add(new JButton("📥 INBOX"));
        foldersPanel.add(new JButton("📤 SENT"));
        foldersPanel.add(new JButton("📝 DRAFTS"));
        foldersPanel.add(new JButton("🗑️ TRASH"));
        add(foldersPanel, BorderLayout.WEST);

        DefaultListModel<String> mailListModel = new DefaultListModel<>();
//        mailListModel.addElement("Asunto: Hola - De: luis@example.com");
//        mailListModel.addElement("Asunto: Recordatorio - De: ana@example.com");
        JList<String> mailList = new JList<>(mailListModel);
        JScrollPane mailScrollPane = new JScrollPane(mailList);
        mailScrollPane.setBorder(BorderFactory.createTitledBorder("Mensajes"));
        add(mailScrollPane, BorderLayout.CENTER);

        JTextArea messageView = new JTextArea("Seleccioná un mensaje para leerlo.");
        messageView.setEditable(false);
        JScrollPane messageScroll = new JScrollPane(messageView);
        messageScroll.setBorder(BorderFactory.createTitledBorder("Lectura"));
        add(messageScroll, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
