package ui.dialogs;

import controllers.ContactsController;
import models.Contact;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ContactsDialog extends JDialog {
    private final ContactsController contactsController;
    private final DefaultListModel<Contact> contactsListModel;
    private final JList<Contact> contactsList;

    public ContactsDialog(Frame owner, ContactsController contactsController) {
        super(owner, "Gestión de Contactos", true);
        this.contactsController = contactsController;
        this.contactsListModel = new DefaultListModel<>();
        this.contactsList = new JList<>(contactsListModel);

        initUI();
        loadContacts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setSize(400, 500);
        setLocationRelativeTo(getOwner());

        // Lista de contactos
        contactsList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            return new JLabel(value.getName() + " " + value.getLastName() + " (" + value.getEmail() + ")");
        });

        JScrollPane scrollPane = new JScrollPane(contactsList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Contactos"));
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton addButton = new JButton("Agregar Contacto");
        addButton.addActionListener(e -> showAddContactDialog());
        buttonPanel.add(addButton);

        JButton deleteButton = new JButton("Eliminar Contacto");
        deleteButton.addActionListener(e -> removeSelectedContact());
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadContacts() {
        contactsListModel.clear();
        List<Contact> contacts = contactsController.getAllContacts();
        for (Contact contact : contacts) {
            contactsListModel.addElement(contact);
        }
    }

    private void showAddContactDialog() {
        JTextField nameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Nombre:"));
        panel.add(nameField);
        panel.add(new JLabel("Apellido:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Agregar Nuevo Contacto", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                contactsController.addContact(
                    nameField.getText(),
                    lastNameField.getText(),
                    emailField.getText()
                );
                loadContacts();
                JOptionPane.showMessageDialog(this, "Contacto agregado exitosamente");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error al agregar el contacto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeSelectedContact() {
        Contact selectedContact = contactsList.getSelectedValue();
        if (selectedContact == null) {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un contacto para eliminar",
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar el contacto " + selectedContact.getName() + " de su agenda?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                contactsController.removeContact(selectedContact);
                loadContacts();
                JOptionPane.showMessageDialog(this, "Contacto eliminado de su agenda exitosamente");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el contacto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 