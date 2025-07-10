package ui.dialogs;

import config.MailServerConfig;

import javax.swing.*;
import java.awt.*;

public class ServerConfigDialog extends JDialog {
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JCheckBox authCheckBox;
    private final JCheckBox starttlsCheckBox;
    private MailServerConfig serverConfig;

    public ServerConfigDialog(Frame parent) {
        super(parent, "Configuración del Servidor de Correo", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Panel principal con GridBagLayout para mejor organización
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Host
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Servidor SMTP:"), gbc);
        gbc.gridx = 1;
        hostField = new JTextField("smtp.palermo.edu", 20);
        mainPanel.add(hostField, gbc);

        // Puerto
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Puerto:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("587", 20);
        mainPanel.add(portField, gbc);

        // Usuario
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // Contraseña
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // Opciones de autenticación
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        authCheckBox = new JCheckBox("Requerir autenticación");
        authCheckBox.setSelected(true);
        mainPanel.add(authCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        starttlsCheckBox = new JCheckBox("Usar STARTTLS");
        starttlsCheckBox.setSelected(true);
        mainPanel.add(starttlsCheckBox, gbc);

        // Panel de información
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        JTextArea infoArea = new JTextArea(
            "Nota: Esta es una simulación. Los correos se enviarán\n" +
            "con el dominio @palermo.edu pero no se conectará\n" +
            "a un servidor real de correo."
        );
        infoArea.setEditable(false);
        infoArea.setBackground(mainPanel.getBackground());
        infoArea.setFont(new Font("Arial", Font.ITALIC, 11));
        mainPanel.add(infoArea, gbc);

        // Botón para ver historial de emails
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        JButton historyButton = new JButton("📧 Ver Historial de Emails");
        historyButton.addActionListener(e -> showEmailHistory());
        mainPanel.add(historyButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            if (validateFields()) {
                saveConfiguration();
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private boolean validateFields() {
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();
        String username = usernameField.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El servidor SMTP es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            int port = Integer.parseInt(portText);
            if (port <= 0 || port > 65535) {
                JOptionPane.showMessageDialog(this, "El puerto debe estar entre 1 y 65535", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El puerto debe ser un número válido", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (authCheckBox.isSelected() && username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El usuario es obligatorio cuando se requiere autenticación", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void saveConfiguration() {
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        boolean auth = authCheckBox.isSelected();
        boolean starttls = starttlsCheckBox.isSelected();

        serverConfig = new MailServerConfig(host, port, username, password, auth, starttls);
    }

    public MailServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(MailServerConfig config) {
        if (config != null) {
            hostField.setText(config.getHost());
            portField.setText(String.valueOf(config.getPort()));
            usernameField.setText(config.getUsername());
            passwordField.setText(config.getPassword());
            authCheckBox.setSelected(config.isAuth());
            starttlsCheckBox.setSelected(config.isStarttls());
        }
    }

    private void showEmailHistory() {
        // Este método se implementará cuando tengamos acceso al EmailHistoryService
        JOptionPane.showMessageDialog(this, 
            "Funcionalidad de historial de emails\n" +
            "disponible en la próxima versión.",
            "Historial de Emails", 
            JOptionPane.INFORMATION_MESSAGE);
    }
} 