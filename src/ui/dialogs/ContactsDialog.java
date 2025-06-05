package ui.dialogs;

import controllers.ContactsController;
import models.User;

import javax.swing.*;
import java.awt.*;

public class ContactsDialog extends JDialog {
    private final ContactsController contactsController;
    private final User currentUser;
    private final JList<User> contactsList;
    private final DefaultListModel<User> listModel;

    public ContactsDialog(Frame parent, ContactsController contactsController, User currentUser) {
        super(parent, "Contactos", true);
        this.contactsController = contactsController;
        this.currentUser = currentUser;
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
        String email = JOptionPane.showInputDialog(this, "Ingrese el email del contacto:");
        if (email != null && !email.trim().isEmpty()) {
            try {
                contactsController.addContact(email.trim());
                loadContacts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al agregar contacto: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
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