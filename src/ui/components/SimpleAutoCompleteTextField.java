package ui.components;

import services.EmailHistoryService;
import models.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Campo de texto con autocompletado simplificado usando JComboBox
 */
public class SimpleAutoCompleteTextField extends JTextField {
    private final EmailHistoryService emailHistoryService;
    private final User currentUser;
    private JComboBox<String> comboBox;
    private boolean isAdjusting = false;

    public SimpleAutoCompleteTextField(EmailHistoryService emailHistoryService, User currentUser) {
        this.emailHistoryService = emailHistoryService;
        this.currentUser = currentUser;
        
        setupComboBox();
        setupListeners();
    }

    private void setupComboBox() {
        comboBox = new JComboBox<>();
        comboBox.setEditable(false);
        comboBox.setVisible(false);
        
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
                    Container parent = getParent();
                    if (parent != null && comboBox.getParent() == null) {
                        parent.add(comboBox);
                    }
                }
            }
        });
    }

    private void setupListeners() {
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isAdjusting) {
                    SwingUtilities.invokeLater(() -> updateSuggestions());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isAdjusting) {
                    SwingUtilities.invokeLater(() -> updateSuggestions());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isAdjusting) {
                    SwingUtilities.invokeLater(() -> updateSuggestions());
                }
            }
        });

        comboBox.addActionListener(e -> {
            if (!isAdjusting && comboBox.getSelectedItem() != null) {
                String selected = comboBox.getSelectedItem().toString();
                isAdjusting = true;
                setText(selected);
                isAdjusting = false;
                hideComboBox();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (comboBox.isVisible()) {
                        comboBox.requestFocus();
                        comboBox.showPopup();
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (comboBox.isVisible() && comboBox.getSelectedItem() != null) {
                        String selected = comboBox.getSelectedItem().toString();
                        isAdjusting = true;
                        setText(selected);
                        isAdjusting = false;
                        hideComboBox();
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideComboBox();
                    e.consume();
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Timer timer = new Timer(150, evt -> hideComboBox());
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    private void updateSuggestions() {
        String text = getText();
        if (text == null || text.trim().isEmpty()) {
            hideComboBox();
            return;
        }

        String[] parts = text.split(",");
        String lastPart = parts[parts.length - 1].trim();
        
        if (lastPart.isEmpty()) {
            hideComboBox();
            return;
        }

        System.out.println("SimpleAutoCompleteTextField: Buscando sugerencias para '" + lastPart + "'");

        List<String> suggestions = emailHistoryService.searchEmails(currentUser, lastPart, 10);
        
        System.out.println("SimpleAutoCompleteTextField: Sugerencias encontradas: " + suggestions);
        
        if (suggestions.isEmpty()) {
            hideComboBox();
            return;
        }

        comboBox.removeAllItems();
        for (String suggestion : suggestions) {
            comboBox.addItem(suggestion);
        }

        showComboBox();
        
        System.out.println("SimpleAutoCompleteTextField: ComboBox mostrado con " + suggestions.size() + " sugerencias");
    }

    private void showComboBox() {
        if (comboBox.isVisible()) {
            return;
        }

        Container parent = getParent();
        if (parent != null && comboBox.getParent() == null) {
            parent.add(comboBox);
        }

        Point location = getLocation();
        Dimension size = getSize();
        
        comboBox.setLocation(location.x, location.y + size.height);
        comboBox.setSize(getWidth(), 150);
        comboBox.setVisible(true);
        
        SwingUtilities.invokeLater(() -> {
            if (comboBox.isVisible()) {
                comboBox.showPopup();
                System.out.println("SimpleAutoCompleteTextField: ComboBox popup mostrado");
            }
        });
        
        System.out.println("SimpleAutoCompleteTextField: ComboBox mostrado en posición: " + location.x + ", " + (location.y + size.height));
    }

    private void hideComboBox() {
        comboBox.setVisible(false);
        comboBox.hidePopup();
    }

    /**
     * Agrega direcciones de correo al historial
     */
    public void addToHistory(List<String> emails) {
        emailHistoryService.addToHistory(currentUser, emails);
    }

    /**
     * Obtiene las direcciones de correo más frecuentes
     */
    public List<String> getFrequentEmails(int limit) {
        return emailHistoryService.getFrequentEmails(currentUser, limit);
    }
} 