package com.groupmatch.app.domain.user;

import jakarta.persistence.*;
import java.time.LocalDate;
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

    private String name;

    @Column(length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDate;

    @Column(nullable = false)
    private Integer searchRadiusKm = 10;

    @Column(nullable = false)
    private Integer dailyLikesLeft = 10;

    private LocalDate likesResetDate;

    @Column(columnDefinition = "TEXT")
    private String avatarBase64;

    // reserved for future geolocation
    private Double latitude;
    private Double longitude;

    protected UserEntity() {}

    public UserEntity(String email, String password) {
        this.uuid = UUID.randomUUID();
        this.email = email;
        this.password = password;
    }

    public void updateProfile(String name, String description, Integer searchRadiusKm) {
        this.name = name;
        this.description = description;
        this.searchRadiusKm = searchRadiusKm;
    }

    public boolean hasLikesLeft(LocalDate today) {
        if (!today.equals(likesResetDate)) {
            dailyLikesLeft = 10;
            likesResetDate = today;
        }
        return dailyLikesLeft > 0;
    }

    public void consumeLike(LocalDate today) {
        if (!today.equals(likesResetDate)) {
            dailyLikesLeft = 10;
            likesResetDate = today;
        }
        dailyLikesLeft--;
    }

    public void setName(String name) { this.name = name; }
    public void setGender(Gender gender) { this.gender = gender; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setPassword(String password) { this.password = password; }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Gender getGender() { return gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public Integer getSearchRadiusKm() { return searchRadiusKm; }
    public Integer getDailyLikesLeft() { return dailyLikesLeft; }
    public LocalDate getLikesResetDate() { return likesResetDate; }
    public String getAvatarBase64() { return avatarBase64; }
    public void setAvatarBase64(String avatarBase64) { this.avatarBase64 = avatarBase64; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
