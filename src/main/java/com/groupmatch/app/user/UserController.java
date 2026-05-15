package com.groupmatch.app.user;

import com.groupmatch.app.group.GroupSummaryResponse;
import com.groupmatch.app.notification.NotificationResponse;
import com.groupmatch.app.notification.service.NotificationService;
import com.groupmatch.app.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/me")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;

    public UserController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getProfile(userDetails.getUsername());
    }

    @PutMapping
    public UserProfileResponse updateProfile(
            @Valid @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return userService.updateProfile(request, userDetails.getUsername());
    }

    @PutMapping("/avatar")
    public UserProfileResponse updateAvatar(
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        return userService.updateAvatar(body.get("imageBase64"), userDetails.getUsername());
    }

    @GetMapping("/groups")
    public List<GroupSummaryResponse> getMyGroups(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getMyGroups(userDetails.getUsername());
    }

    @GetMapping("/notifications")
    public List<NotificationResponse> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return notificationService.getNotifications(userDetails.getUsername());
    }
}
