package ui.dialogs;

import services.EmailHistoryService;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Diálogo para mostrar el historial de direcciones de correo utilizadas
 */
public class EmailHistoryDialog extends JDialog {
    private final EmailHistoryService emailHistoryService;
    private final User currentUser;
    private final JList<String> emailList;
    private final DefaultListModel<String> listModel;

    public EmailHistoryDialog(Frame parent, EmailHistoryService emailHistoryService, User currentUser) {
        super(parent, "Historial de Direcciones de Correo", true);
        this.emailHistoryService = emailHistoryService;
        this.currentUser = currentUser;
        this.listModel = new DefaultListModel<>();
        this.emailList = new JList<>(listModel);

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initUI();
        loadEmailHistory();
    }

    private void initUI() {
        // Panel superior con información
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información del Historial"));
        
        Map<String, Object> stats = emailHistoryService.getHistoryStats(currentUser);
        int totalEmails = (Integer) stats.getOrDefault("totalEmails", 0);
        
        JLabel infoLabel = new JLabel("Total de direcciones utilizadas: " + totalEmails);
        infoPanel.add(infoLabel, BorderLayout.WEST);
        
        add(infoPanel, BorderLayout.NORTH);

        // Lista de emails
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Direcciones Más Frecuentes"));
        
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(emailList);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(listPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton refreshButton = new JButton("🔄 Actualizar");
        refreshButton.addActionListener(e -> loadEmailHistory());
        
        JButton clearButton = new JButton("🗑️ Limpiar Historial");
        clearButton.addActionListener(e -> clearHistory());
        
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadEmailHistory() {
        listModel.clear();
        
        List<String> frequentEmails = emailHistoryService.getFrequentEmails(currentUser, 20);
        
        if (frequentEmails.isEmpty()) {
            listModel.addElement("No hay direcciones en el historial");
        } else {
            for (int i = 0; i < frequentEmails.size(); i++) {
                String email = frequentEmails.get(i);
                Map<String, Object> stats = emailHistoryService.getHistoryStats(currentUser);
                @SuppressWarnings("unchecked")
                List<String> topEmails = (List<String>) stats.get("topEmails");
                
                String displayText = String.format("%d. %s", i + 1, email);
                if (topEmails != null && topEmails.contains(email)) {
                    displayText += " ⭐";
                }
                
                listModel.addElement(displayText);
            }
        }
    }

    private void clearHistory() {
        int result = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea limpiar todo el historial de direcciones?\n" +
            "Esta acción no se puede deshacer.",
            "Confirmar Limpieza",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            emailHistoryService.clearUserHistory(currentUser);
            loadEmailHistory();
            JOptionPane.showMessageDialog(this,
                "Historial limpiado exitosamente",
                "Historial Limpiado",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
} 