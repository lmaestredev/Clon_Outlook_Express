package ui.dialogs;

import controllers.UserController;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class LoginDialog extends JDialog {
    private final JTextField emailField;
    private User loggedUser;
    private final UserController userController;

    public LoginDialog(Frame parent, UserController userController) {
        super(parent, "Iniciar Sesión", true);
        this.userController = userController;
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
        JButton registerButton = new JButton("Registrarse");

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

        registerButton.addActionListener(e -> showRegisterDialog());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showRegisterDialog() {
        JTextField nameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailFieldReg = new JTextField();
        JPanel regPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        regPanel.add(new JLabel("Nombre:"));
        regPanel.add(nameField);
        regPanel.add(new JLabel("Apellido:"));
        regPanel.add(lastNameField);
        regPanel.add(new JLabel("Email:"));
        regPanel.add(emailFieldReg);

        int result = JOptionPane.showConfirmDialog(this, regPanel, "Registro de Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailFieldReg.getText().trim();
            if (name.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (userController.findByEmail(email).isPresent()) {
                JOptionPane.showMessageDialog(this, "El email ya está registrado", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User newUser = new User(UUID.randomUUID(), name, lastName, email);
            userController.save(newUser);
            loggedUser = newUser;
            JOptionPane.showMessageDialog(this, "Usuario registrado exitosamente. ¡Bienvenido, " + name + "!", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    public User getLoggedUser() {
        return loggedUser;
    }
} 