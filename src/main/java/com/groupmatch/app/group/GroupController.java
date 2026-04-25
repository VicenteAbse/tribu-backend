package com.groupmatch.app.group;

import com.groupmatch.app.group.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDetailResponse createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.create(request, userDetails.getUsername());
    }

    @GetMapping("/discover")
    public Page<GroupDiscoveryResponse> discover(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return groupService.discover(userDetails.getUsername(), pageable);
    }

    @GetMapping("/{groupId}")
    public GroupDetailResponse getDetail(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getDetail(groupId, userDetails.getUsername());
    }

    @PostMapping("/{groupId}/swipe")
    public SwipeResponse swipe(
            @PathVariable Long groupId,
            @Valid @RequestBody SwipeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.swipe(groupId, request, userDetails.getUsername());
    }
}
