package ui.dialogs;

import controllers.MailController;
import models.Mail;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ComposeMailDialog extends JDialog {
    private final JTextField toField;
    private final JTextField ccField;
    private final JTextField bccField;
    private final JTextField subjectField;
    private final JTextArea messageArea;
    private final MailController mailController;
    private final User currentUser;
    private final List<User> allUsers;
    private Mail draftMail;
    private Timer autoSaveTimer;

    public ComposeMailDialog(Frame parent, MailController mailController, User currentUser, List<User> allUsers) {
        super(parent, "Redactar Correo", true);
        this.mailController = mailController;
        this.currentUser = currentUser;
        this.allUsers = allUsers;

        setSize(600, 400);
        setLocationRelativeTo(parent);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel superior para campos
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        fieldsPanel.add(new JLabel("Para:"));
        toField = new JTextField();
        fieldsPanel.add(toField);

        fieldsPanel.add(new JLabel("CC:"));
        ccField = new JTextField();
        fieldsPanel.add(ccField);

        fieldsPanel.add(new JLabel("BCC:"));
        bccField = new JTextField();
        fieldsPanel.add(bccField);

        fieldsPanel.add(new JLabel("Asunto:"));
        subjectField = new JTextField();
        fieldsPanel.add(subjectField);

        mainPanel.add(fieldsPanel, BorderLayout.NORTH);

        // Área de mensaje
        messageArea = new JTextArea();
        JScrollPane messageScroll = new JScrollPane(messageArea);
        mainPanel.add(messageScroll, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveDraftButton = new JButton("Guardar Borrador");
        JButton sendButton = new JButton("Enviar");
        JButton cancelButton = new JButton("Cancelar");

        saveDraftButton.addActionListener(e -> {
            saveDraftAndClose();
            stopAutoSaveTimer();
        });
        sendButton.addActionListener(e -> sendMail());
        cancelButton.addActionListener(e -> {
            stopAutoSaveTimer();
            dispose();
        });

        buttonPanel.add(saveDraftButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        autoSaveTimer = new Timer(30000, e -> saveDraft()); // Guarda cada 30 segundos sin cerrar
        autoSaveTimer.setRepeats(true);
        autoSaveTimer.start();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveDraftAndClose();
                stopAutoSaveTimer();
            }
        });

        add(mainPanel);
    }

    private void stopAutoSaveTimer() {
        if (autoSaveTimer != null) {
            autoSaveTimer.stop();
            autoSaveTimer = null;
        }
    }

    private void saveDraft() {
        String to = toField.getText().trim();
        String cc = ccField.getText().trim();
        String bcc = bccField.getText().trim();
        String subject = subjectField.getText().trim();
        String message = messageArea.getText().trim();

        if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty() && subject.isEmpty() && message.isEmpty()) {
            return;
        }

        try {
            if (draftMail == null) {
                draftMail = mailController.createDraft(currentUser, to, cc, bcc, subject, message);
            } else {
                mailController.updateDraft(draftMail, to, cc, bcc, subject, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al guardar el borrador:\n" + e.getMessage() + "\n\n" + e.getClass().getName(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveDraftAndClose() {
        saveDraft();
        dispose();
    }

    private void sendMail() {
        String to = toField.getText().trim();
        String cc = ccField.getText().trim();
        String bcc = bccField.getText().trim();
        String subject = subjectField.getText().trim();
        String message = messageArea.getText().trim();

        if (to.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un destinatario", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (subject.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un asunto", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un mensaje", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            mailController.sendMail(currentUser, to, cc, bcc, subject, message);
            if (draftMail != null) {
                mailController.deleteDraft(draftMail);
            }
            stopAutoSaveTimer();
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al enviar el correo: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadDraft(Mail draft) {
        this.draftMail = draft;
        toField.setText(draft.getRecipients().stream()
                .map(User::getEmail)
                .collect(Collectors.joining(", ")));
        ccField.setText(draft.getCc().stream()
                .map(User::getEmail)
                .collect(Collectors.joining(", ")));
        bccField.setText(draft.getBcc().stream()
                .map(User::getEmail)
                .collect(Collectors.joining(", ")));
        subjectField.setText(draft.getSubject());
        messageArea.setText(draft.getMessage());
    }
}

