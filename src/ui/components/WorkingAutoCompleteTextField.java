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
 * Campo de texto con autocompletado funcional
 */
public class WorkingAutoCompleteTextField extends JTextField {
    private final EmailHistoryService emailHistoryService;
    private final User currentUser;
    private JList<String> suggestionList;
    private JWindow popup;
    private DefaultListModel<String> listModel;
    private boolean isAdjusting = false;

    public WorkingAutoCompleteTextField(EmailHistoryService emailHistoryService, User currentUser) {
        this.emailHistoryService = emailHistoryService;
        this.currentUser = currentUser;
        this.listModel = new DefaultListModel<>();
        this.suggestionList = new JList<>(listModel);
        
        setupPopup();
        setupListeners();
    }

    private void setupPopup() {

        popup = new JWindow();
        popup.setLayout(new BorderLayout());
        
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFont(getFont());
        suggestionList.setBackground(Color.WHITE);
        suggestionList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setPreferredSize(new Dimension(300, 120));
        popup.add(scrollPane, BorderLayout.CENTER);
        
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    selectSuggestion();
                }
            }
        });
        
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

        System.out.println("WorkingAutoCompleteTextField: Buscando sugerencias para '" + lastPart + "'");

        List<String> suggestions = emailHistoryService.searchEmails(currentUser, lastPart, 8);
        
        System.out.println("WorkingAutoCompleteTextField: Sugerencias encontradas: " + suggestions);
        
        if (suggestions.isEmpty()) {
            hidePopup();
            return;
        }

        listModel.clear();
        for (String suggestion : suggestions) {
            listModel.addElement(suggestion);
        }

        showPopup();
        
        if (suggestionList.getModel().getSize() > 0) {
            suggestionList.setSelectedIndex(0);
        }
    }

    private void showPopup() {
        if (popup.isVisible()) {
            return;
        }

        Point location = getLocationOnScreen();
        Dimension size = getSize();
        
        int x = location.x;
        int y = location.y + size.height;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (y + 120 > screenSize.height) {
            y = location.y - 120;
        }
        
        if (popup.getOwner() == null) {
            Window owner = SwingUtilities.getWindowAncestor(this);
            if (owner != null) {
                popup = new JWindow(owner);
                popup.setLayout(new BorderLayout());
                JScrollPane scrollPane = new JScrollPane(suggestionList);
                scrollPane.setPreferredSize(new Dimension(300, 120));
                popup.add(scrollPane, BorderLayout.CENTER);
            }
        }
        
        popup.setLocation(x, y);
        popup.pack();
        popup.setVisible(true);
        popup.toFront();
        
        System.out.println("WorkingAutoCompleteTextField: Popup mostrado en posición: " + x + ", " + y);
        System.out.println("WorkingAutoCompleteTextField: Popup visible: " + popup.isVisible());
        System.out.println("WorkingAutoCompleteTextField: Popup owner: " + popup.getOwner());
    }

    private void hidePopup() {
        if (popup != null) {
            popup.setVisible(false);
        }
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