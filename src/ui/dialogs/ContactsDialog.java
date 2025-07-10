package ui.dialogs;

import controllers.ContactsController;
import models.User;
import services.EmailHistoryService;
import ui.components.AutoCompleteTextField;

import javax.swing.*;
import java.awt.*;

public class ContactsDialog extends JDialog {
    private final ContactsController contactsController;
    private final User currentUser;
    private final JList<User> contactsList;
    private final DefaultListModel<User> listModel;
    private final EmailHistoryService emailHistoryService;

    public ContactsDialog(Frame parent, ContactsController contactsController, User currentUser, EmailHistoryService emailHistoryService) {
        super(parent, "Contactos", true);
        this.contactsController = contactsController;
        this.currentUser = currentUser;
        this.emailHistoryService = emailHistoryService;
        this.listModel = new DefaultListModel<>();
        this.contactsList = new JList<>(listModel);

        setSize(400, 300);
        setLocationRelativeTo(parent);
        initUI();
        loadContacts();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Panel de lista de contactos
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(contactsList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Agregar");
        JButton removeButton = new JButton("Eliminar");
        JButton closeButton = new JButton("Cerrar");

        addButton.addActionListener(e -> showAddContactDialog());
        removeButton.addActionListener(e -> removeSelectedContact());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadContacts() {
        listModel.clear();
        contactsController.getAllContacts().forEach(listModel::addElement);
    }

    private void showAddContactDialog() {
        // Crear un diálogo personalizado con autocompletado
        JDialog dialog = new JDialog(this, "Agregar Contacto", true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Email:"));
        AutoCompleteTextField emailField = new AutoCompleteTextField(emailHistoryService, currentUser);
        panel.add(emailField);

        dialog.add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Agregar");
        JButton cancelButton = new JButton("Cancelar");

        addButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (!email.isEmpty()) {
                try {
                    contactsController.addContact(email);
                    // Agregar al historial de emails
                    emailHistoryService.addToHistory(currentUser, java.util.Arrays.asList(email));
                    loadContacts();
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Error al agregar contacto: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Por favor ingrese un email válido",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void removeSelectedContact() {
        User selectedContact = contactsList.getSelectedValue();
        if (selectedContact != null) {
            try {
                contactsController.removeContact(selectedContact);
                loadContacts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar contacto: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 