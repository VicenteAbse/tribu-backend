package com.groupmatch.app.user;

import com.groupmatch.app.domain.user.Gender;
import com.groupmatch.app.domain.user.UserEntity;

import java.time.LocalDate;
import java.util.UUID;

public class UserProfileResponse {

    private UUID uuid;
    private String email;
    private String name;
    private String description;
    private Gender gender;
    private LocalDate birthDate;
    private Integer searchRadiusKm;
    private Integer dailyLikesLeft;
    private String avatarBase64;

    public UserProfileResponse(UserEntity user) {
        this.uuid = user.getUuid();
        this.email = user.getEmail();
        this.name = user.getName();
        this.description = user.getDescription();
        this.gender = user.getGender();
        this.birthDate = user.getBirthDate();
        this.searchRadiusKm = user.getSearchRadiusKm();
        this.dailyLikesLeft = user.getDailyLikesLeft();
        this.avatarBase64 = user.getAvatarBase64();
    }

    public UUID getUuid() { return uuid; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Gender getGender() { return gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public Integer getSearchRadiusKm() { return searchRadiusKm; }
    public Integer getDailyLikesLeft() { return dailyLikesLeft; }
    public String getAvatarBase64() { return avatarBase64; }
}
