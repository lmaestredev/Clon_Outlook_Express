package ui.components;

import services.EmailHistoryService;
import models.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;

/**
 * Campo de texto con autocompletado para direcciones de correo
 */
public class AutoCompleteTextField extends JTextField {
    private final EmailHistoryService emailHistoryService;
    private final User currentUser;
    private JWindow popup;
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    private boolean isAdjusting = false;
    private static final int MAX_SUGGESTIONS = 8;

    public AutoCompleteTextField(EmailHistoryService emailHistoryService, User currentUser) {
        this.emailHistoryService = emailHistoryService;
        this.currentUser = currentUser;
        this.listModel = new DefaultListModel<>();
        this.suggestionList = new JList<>(listModel);
        
        setupPopup();
        setupListeners();
    }

    private void setupPopup() {
        // Crear el popup con el frame padre como owner
        Window owner = SwingUtilities.getWindowAncestor(this);
        popup = new JWindow(owner);
        popup.setLayout(new BorderLayout());
        
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFont(getFont());
        suggestionList.setBackground(Color.WHITE);
        suggestionList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        popup.add(scrollPane, BorderLayout.CENTER);
        
        popup.setAlwaysOnTop(true);
        
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    selectSuggestion();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hidePopup();
                }
            }
        });
        
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    selectSuggestion();
                }
            }
        });
    }

    private void setupListeners() {
        // Listener para cambios en el texto
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

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (popup.isVisible()) {
                        suggestionList.requestFocus();
                        if (suggestionList.getSelectedIndex() < suggestionList.getModel().getSize() - 1) {
                            suggestionList.setSelectedIndex(suggestionList.getSelectedIndex() + 1);
                        }
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (popup.isVisible()) {
                        suggestionList.requestFocus();
                        if (suggestionList.getSelectedIndex() > 0) {
                            suggestionList.setSelectedIndex(suggestionList.getSelectedIndex() - 1);
                        }
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (popup.isVisible()) {
                        selectSuggestion();
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hidePopup();
                    e.consume();
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Pequeño delay para permitir clics en la lista
                Timer timer = new Timer(150, evt -> hidePopup());
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    private void updateSuggestions() {
        String text = getText();
        if (text == null || text.trim().isEmpty()) {
            hidePopup();
            return;
        }

        String[] parts = text.split(",");
        String lastPart = parts[parts.length - 1].trim();
        
        if (lastPart.isEmpty()) {
            hidePopup();
            return;
        }

        System.out.println("AutoCompleteTextField: Buscando sugerencias para '" + lastPart + "'");

        // Buscar sugerencias
        List<String> suggestions = emailHistoryService.searchEmails(currentUser, lastPart, MAX_SUGGESTIONS);
        
        System.out.println("AutoCompleteTextField: Sugerencias encontradas: " + suggestions);
        
        if (suggestions.isEmpty()) {
            System.out.println("AutoCompleteTextField: No hay sugerencias, ocultando popup");
            hidePopup();
            return;
        }

        listModel.clear();
        for (String suggestion : suggestions) {
            listModel.addElement(suggestion);
        }

        System.out.println("AutoCompleteTextField: Mostrando popup con " + suggestions.size() + " sugerencias");
        
        showPopup();
        
        if (suggestionList.getModel().getSize() > 0) {
            suggestionList.setSelectedIndex(0);
        }
    }

    private void showPopup() {
        if (popup.isVisible()) {
            System.out.println("AutoCompleteTextField: Popup ya está visible");
            return;
        }

        // Posicionar el popup debajo del campo de texto
        Point location = getLocationOnScreen();
        Dimension size = getSize();
        
        int x = location.x;
        int y = location.y + size.height;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (y + popup.getPreferredSize().height > screenSize.height) {
            y = location.y - popup.getPreferredSize().height;
        }
        
        popup.setLocation(x, y);
        popup.pack();
        popup.setVisible(true);
        popup.toFront();
        popup.requestFocus();
        
        System.out.println("AutoCompleteTextField: Popup mostrado en posición: " + x + ", " + y);
        System.out.println("AutoCompleteTextField: Tamaño del popup: " + popup.getSize());
    }

    private void hidePopup() {
        popup.setVisible(false);
    }

    private void selectSuggestion() {
        String selected = suggestionList.getSelectedValue();
        if (selected != null) {
            isAdjusting = true;
            
            String text = getText();
            String[] parts = text.split(",");
            
            if (parts.length > 1) {
                parts[parts.length - 1] = selected;
                setText(String.join(",", parts));
            } else {
                setText(selected);
            }
            
            // Mover el cursor al final
            setCaretPosition(getText().length());
            
            isAdjusting = false;
            hidePopup();
        }
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