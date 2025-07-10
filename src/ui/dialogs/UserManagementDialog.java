package ui.dialogs;

import models.User;
import models.UserRole;
import persistence.dao.UserDao;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Diálogo para gestionar usuarios (solo para administradores)
 */
public class UserManagementDialog extends JDialog {
    private final UserDao userDao;
    private final JList<User> userList;
    private final DefaultListModel<User> listModel;

    public UserManagementDialog(Frame parent, UserDao userDao) {
        super(parent, "Gestión de Usuarios", true);
        this.userDao = userDao;
        this.listModel = new DefaultListModel<>();
        this.userList = new JList<>(listModel);

        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initUI();
        loadUsers();
    }

    private void initUI() {
        // Panel de lista de usuarios
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Usuarios del Sistema"));

        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String displayText = String.format("%s %s (%s) - %s", 
                value.getName(), 
                value.getLastName(), 
                value.getEmail(), 
                value.getRole());
            
            JLabel label = new JLabel(displayText);
            if (value.getRole() == UserRole.ADMIN) {
                label.setForeground(Color.RED);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }
            return label;
        });

        JScrollPane scrollPane = new JScrollPane(userList);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        add(listPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton changeRoleButton = new JButton("🔄 Cambiar Rol");
        changeRoleButton.addActionListener(e -> changeUserRole());

        JButton refreshButton = new JButton("🔄 Actualizar");
        refreshButton.addActionListener(e -> loadUsers());

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(changeRoleButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        listModel.clear();
        List<User> users = userDao.findAll();
        for (User user : users) {
            listModel.addElement(user);
        }
    }

    private void changeUserRole() {
        User selectedUser = userList.getSelectedValue();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un usuario",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear opciones de rol
        UserRole[] roles = UserRole.values();
        String[] roleNames = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].name();
        }

        // Mostrar diálogo de selección
        String selectedRole = (String) JOptionPane.showInputDialog(this,
            "Seleccione el nuevo rol para " + selectedUser.getEmail() + ":",
            "Cambiar Rol",
            JOptionPane.QUESTION_MESSAGE,
            null,
            roleNames,
            selectedUser.getRole().name());

        if (selectedRole != null && !selectedRole.equals(selectedUser.getRole().name())) {
            try {
                UserRole newRole = UserRole.valueOf(selectedRole);
                userDao.updateRole(selectedUser.getId(), newRole);
                
                selectedUser.setRole(newRole);
                userList.repaint();
                
                JOptionPane.showMessageDialog(this,
                    "Rol actualizado exitosamente",
                    "Rol Actualizado",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al actualizar el rol: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 