package ui.dialogs;

import controllers.MailController;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ComposeMailDialog extends JDialog {

    private final MailController mailController;
    private final User currentUser;
    private final List<User> allUsers;

    public ComposeMailDialog(Frame owner, MailController mailController, User currentUser, List<User> allUsers) {
        super(owner, "Redactar correo", true);
        this.mailController = mailController;
        this.currentUser = currentUser;
        this.allUsers = allUsers;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JTextField toField = new JTextField();
        JTextField subjectField = new JTextField();
        JTextArea messageArea = new JTextArea(10, 30);

        JPanel formPanel = new JPanel(new GridLayout(3, 1));
        formPanel.add(new JLabel("Para (email separados por coma):"));
        formPanel.add(toField);
        formPanel.add(new JLabel("Asunto:"));
        formPanel.add(subjectField);

        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> {
            try {
                String[] recipients = toField.getText().split(",");
                List<User> toUsers = allUsers.stream()
                        .filter(u -> Arrays.asList(recipients).contains(u.getEmail()))
                        .toList();

                if (toUsers.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Por favor ingrese al menos un destinatario válido",
                        "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                mailController.sendMail(toUsers, subjectField.getText(), messageArea.getText());
                JOptionPane.showMessageDialog(this, "Correo enviado correctamente");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error al enviar el correo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(sendButton, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
    }
}

