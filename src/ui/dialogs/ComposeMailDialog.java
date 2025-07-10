package ui.dialogs;

import controllers.MailController;
import models.Mail;
import models.User;
import services.EmailHistoryService;
import ui.components.AutoCompleteTextField;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ComposeMailDialog extends JDialog {
    private final AutoCompleteTextField toField;
    private final AutoCompleteTextField ccField;
    private final AutoCompleteTextField bccField;
    private final JTextField subjectField;
    private final JTextArea messageArea;
    private final MailController mailController;
    private final User currentUser;
    private final List<User> allUsers;
    private final EmailHistoryService emailHistoryService;
    private Mail draftMail;
    private Timer autoSaveTimer;

    public ComposeMailDialog(Frame parent, MailController mailController, User currentUser, List<User> allUsers, EmailHistoryService emailHistoryService) {
        super(parent, "Redactar Correo", true);
        this.mailController = mailController;
        this.currentUser = currentUser;
        this.allUsers = allUsers;
        this.emailHistoryService = emailHistoryService;

        setSize(600, 400);
        setLocationRelativeTo(parent);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel superior para campos
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        fieldsPanel.add(new JLabel("Para:"));
        toField = new AutoCompleteTextField(emailHistoryService, currentUser);
        fieldsPanel.add(toField);

        fieldsPanel.add(new JLabel("CC:"));
        ccField = new AutoCompleteTextField(emailHistoryService, currentUser);
        fieldsPanel.add(ccField);

        fieldsPanel.add(new JLabel("BCC:"));
        bccField = new AutoCompleteTextField(emailHistoryService, currentUser);
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
            
            // Agregar direcciones al historial de autocompletado
            addEmailsToHistory(to, cc, bcc);
            
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

    /**
     * Agrega las direcciones de correo utilizadas al historial
     */
    private void addEmailsToHistory(String to, String cc, String bcc) {
        List<String> allEmails = new ArrayList<>();
        
        if (to != null && !to.trim().isEmpty()) {
            allEmails.addAll(Arrays.asList(to.split(",")));
        }
        if (cc != null && !cc.trim().isEmpty()) {
            allEmails.addAll(Arrays.asList(cc.split(",")));
        }
        if (bcc != null && !bcc.trim().isEmpty()) {
            allEmails.addAll(Arrays.asList(bcc.split(",")));
        }
        
        // Limpiar y agregar al historial
        List<String> cleanEmails = allEmails.stream()
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toList());
        
        emailHistoryService.addToHistory(currentUser, cleanEmails);
    }
}

