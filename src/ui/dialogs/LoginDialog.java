package ui.dialogs;

import controllers.UserController;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LoginDialog extends JDialog {
    private final JTextField emailField;
    private User loggedUser;

    public LoginDialog(Frame parent, UserController userController) {
        super(parent, "Iniciar Sesión", true);
        setSize(350, 150);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        add(panel, BorderLayout.CENTER);

        JButton loginButton = new JButton("Ingresar");
        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese un email", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Optional<User> userOpt = userController.findByEmail(email);
            if (userOpt.isPresent()) {
                loggedUser = userOpt.get();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loginButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public User getLoggedUser() {
        return loggedUser;
    }
} 