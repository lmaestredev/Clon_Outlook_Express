package services;

import models.User;
import persistence.dao.UserDao;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para manejar el historial de direcciones de correo utilizadas
 * Permite autocompletar direcciones basándose en el historial del usuario
 */
public class EmailHistoryService {
    private final UserDao userDao;
    private final Map<String, Set<String>> userEmailHistory;
    private final Map<String, Integer> emailFrequency;

    public EmailHistoryService(UserDao userDao) {
        this.userDao = userDao;
        this.userEmailHistory = new HashMap<>();
        this.emailFrequency = new HashMap<>();
    }

    /**
     * Agrega direcciones de correo al historial del usuario
     */
    public void addToHistory(User user, List<String> emails) {
        if (user == null || emails == null) {
            return;
        }

        String userId = user.getId().toString();
        userEmailHistory.computeIfAbsent(userId, k -> new HashSet<>());
        
        for (String email : emails) {
            if (email != null && !email.trim().isEmpty()) {
                String cleanEmail = email.trim().toLowerCase();
                userEmailHistory.get(userId).add(cleanEmail);

                emailFrequency.merge(cleanEmail, 1, Integer::sum);
            }
        }
    }

    /**
     * Obtiene las direcciones de correo más utilizadas por el usuario
     */
    public List<String> getFrequentEmails(User user, int limit) {
        if (user == null) {
            return new ArrayList<>();
        }

        String userId = user.getId().toString();
        Set<String> userEmails = userEmailHistory.getOrDefault(userId, new HashSet<>());
        
        return userEmails.stream()
                .sorted((email1, email2) -> {
                    int freq1 = emailFrequency.getOrDefault(email1, 0);
                    int freq2 = emailFrequency.getOrDefault(email2, 0);
                    return Integer.compare(freq2, freq1); // Orden descendente por frecuencia
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Busca direcciones de correo que coincidan con el patrón ingresado
     */
    public List<String> searchEmails(User user, String pattern, int limit) {
        if (user == null || pattern == null) {
            return new ArrayList<>();
        }

        String lowerPattern = pattern.toLowerCase().trim();
        
        if (lowerPattern.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> allUsers = userDao.findAll();
        
        // Buscar usuarios que coincidan con el patrón (que comiencen con el string)
        List<String> matches = allUsers.stream()
                .map(User::getEmail)
                .filter(email -> email != null && email.toLowerCase().startsWith(lowerPattern))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());

        // Si no hay suficientes resultados con startsWith, buscar con contains
        if (matches.size() < limit) {
            List<String> containsMatches = allUsers.stream()
                    .map(User::getEmail)
                    .filter(email -> email != null && email.toLowerCase().contains(lowerPattern))
                    .filter(email -> !matches.contains(email))
                    .distinct()
                    .limit(limit - matches.size())
                    .collect(Collectors.toList());
            
            matches.addAll(containsMatches);
        }

        return matches;
    }


    /**
     * Obtiene todas las direcciones de correo del historial del usuario
     */
    public Set<String> getAllUserEmails(User user) {
        if (user == null) {
            return new HashSet<>();
        }

        String userId = user.getId().toString();
        return new HashSet<>(userEmailHistory.getOrDefault(userId, new HashSet<>()));
    }

    /**
     * Limpia el historial de un usuario específico
     */
    public void clearUserHistory(User user) {
        if (user == null) {
            return;
        }

        String userId = user.getId().toString();
        userEmailHistory.remove(userId);
    }

    /**
     * Obtiene estadísticas del historial
     */
    public Map<String, Object> getHistoryStats(User user) {
        Map<String, Object> stats = new HashMap<>();
        
        if (user != null) {
            String userId = user.getId().toString();
            Set<String> userEmails = userEmailHistory.getOrDefault(userId, new HashSet<>());
            
            stats.put("totalEmails", userEmails.size());
            stats.put("totalFrequency", emailFrequency.size());
            
            // Top 5 emails más frecuentes
            List<String> topEmails = getFrequentEmails(user, 5);
            stats.put("topEmails", topEmails);
        }
        
        return stats;
    }
} 