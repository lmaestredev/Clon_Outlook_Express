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
 * Campo de texto con autocompletado usando JComboBox
 */
public class ComboBoxAutoCompleteTextField extends JComboBox<String> {
    private final EmailHistoryService emailHistoryService;
    private final User currentUser;
    private boolean isAdjusting = false;
    private JTextField editor;

    public ComboBoxAutoCompleteTextField(EmailHistoryService emailHistoryService, User currentUser) {
        this.emailHistoryService = emailHistoryService;
        this.currentUser = currentUser;
        
        setEditable(true);
        setMaximumRowCount(8);
        
        // Obtener el editor
        editor = (JTextField) getEditor().getEditorComponent();
        
        setupListeners();
    }

    private void setupListeners() {
        // Listener para cambios en el texto del editor
        editor.getDocument().addDocumentListener(new DocumentListener() {
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

        // Listener para teclas
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (getSelectedItem() != null) {
                        // Completar la sugerencia seleccionada
                        String selected = getSelectedItem().toString();
                        isAdjusting = true;
                        editor.setText(selected);
                        editor.setCaretPosition(selected.length());
                        isAdjusting = false;
                    }
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hidePopup();
                    e.consume();
                }
                // Permitir todas las demás teclas normalmente (incluyendo Backspace y Delete)
            }
        });

        // Listener para selección de item
        addActionListener(e -> {
            if (getSelectedItem() != null && !isAdjusting) {
                String selected = getSelectedItem().toString();
                isAdjusting = true;
                editor.setText(selected);
                editor.setCaretPosition(selected.length());
                isAdjusting = false;
            }
        });
    }

    private void updateSuggestions() {
        String text = editor.getText();
        if (text == null || text.trim().isEmpty()) {
            hidePopup();
            return;
        }

        // Extraer la última dirección de correo del texto
        String[] parts = text.split(",");
        String lastPart = parts[parts.length - 1].trim();
        
        if (lastPart.isEmpty()) {
            hidePopup();
            return;
        }

        System.out.println("ComboBoxAutoCompleteTextField: Buscando sugerencias para '" + lastPart + "'");

        // Buscar sugerencias
        List<String> suggestions = emailHistoryService.searchEmails(currentUser, lastPart, 8);
        
        System.out.println("ComboBoxAutoCompleteTextField: Sugerencias encontradas: " + suggestions);
        
        if (suggestions.isEmpty()) {
            hidePopup();
            return;
        }

        // Actualizar la lista de sugerencias
        isAdjusting = true;
        removeAllItems();
        for (String suggestion : suggestions) {
            addItem(suggestion);
        }
        isAdjusting = false;

        // Mostrar el popup
        showPopup();
        
        // Seleccionar la primera sugerencia para navegación con teclado
        if (getItemCount() > 0) {
            setSelectedIndex(0);
        }
    }

    /**
     * Obtiene el texto actual del editor
     */
    public String getText() {
        return editor.getText();
    }

    /**
     * Establece el texto del editor
     */
    public void setText(String text) {
        editor.setText(text);
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