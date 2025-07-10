package models;

import java.util.UUID;

public class User {

    private UUID id;
    private String name;
    private String lastName;
    private String email;
    private UserRole role;

    public User(UUID id, String name, String lastName, String email) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.role = UserRole.USER; // Por defecto todos los usuarios tienen rol USER
    }

    public User(UUID id, String name, String lastName, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return this.getEmail();
    }
}
