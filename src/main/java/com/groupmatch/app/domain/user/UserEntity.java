package com.groupmatch.app.domain.user;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    protected UserEntity() {}

    public UserEntity(String email, String password) {
        this.uuid = UUID.randomUUID();
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
