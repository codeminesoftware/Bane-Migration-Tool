package com.inpart.bane.model;

public class Client {
    private Long id;
    private String name;
    private String email;
    private boolean migrated;

    public Client(Long id, String name, String email, boolean migrated) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.migrated = migrated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isMigrated() {
        return migrated;
    }

    public void setMigrated(boolean migrated) {
        this.migrated = migrated;
    }
}

