package ui.dialogs;

import controllers.ContactsController;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ContactsDialog extends JDialog {

    private final DefaultListModel<User> contactListModel = new DefaultListModel<>();
    private final JList<User> contactList = new JList<>(contactListModel);
    private final ContactsController contactsController;

    public ContactsDialog(Frame owner, ContactsController contactsController) {
        super(owner, "Contactos", true);
        this.contactsController = contactsController;

        initUI();
        loadContacts();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Lista de contactos
        contactList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            return new JLabel(value.getEmail());
        });

        JScrollPane scrollPane = new JScrollPane(contactList);
        add(scrollPane, BorderLayout.CENTER);

        // Botones
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Agregar");
        JButton removeButton = new JButton("Eliminar");

        addButton.addActionListener(e -> addNewContact());
        removeButton.addActionListener(e -> removeSelectedContact());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void loadContacts() {
        contactListModel.clear();
        List<User> contacts = contactsController.getAllContacts();
        for (User contact : contacts) {
            contactListModel.addElement(contact);
        }
    }

    private void addNewContact() {
        String email = JOptionPane.showInputDialog(this, "Ingrese el email del contacto:");
        if (email != null && !email.trim().isEmpty()) {
            try {
                contactsController.addContact(email);
                loadContacts();
                JOptionPane.showMessageDialog(this, "Contacto agregado exitosamente");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error al agregar contacto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeSelectedContact() {
        User selectedContact = contactList.getSelectedValue();
        if (selectedContact != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea eliminar este contacto de su agenda?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    contactsController.removeContact(selectedContact);
                    loadContacts();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error al eliminar contacto: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un contacto para eliminar",
                "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
} 