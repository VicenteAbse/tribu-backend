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

import java.util.List;

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

    @PutMapping("/{groupId}")
    public GroupDetailResponse updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.updateGroup(groupId, request, userDetails.getUsername());
    }

    @PostMapping("/{groupId}/swipe")
    public SwipeResponse swipe(
            @PathVariable Long groupId,
            @Valid @RequestBody SwipeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.swipe(groupId, request, userDetails.getUsername());
    }

    @GetMapping("/{groupId}/members")
    public List<MemberResponse> getMembers(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getMembers(groupId, userDetails.getUsername());
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        groupService.removeMember(groupId, memberId, userDetails.getUsername());
    }

    @PostMapping("/{groupId}/members/{memberId}/promote")
    public MemberResponse promoteMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.promoteMember(groupId, memberId, userDetails.getUsername());
    }

    @PostMapping("/{groupId}/members/{memberId}/mute")
    public MemberResponse muteMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.muteMember(groupId, memberId, userDetails.getUsername());
    }

    @GetMapping("/{groupId}/join-requests")
    public List<JoinRequestResponse> getJoinRequests(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getJoinRequests(groupId, userDetails.getUsername());
    }

    @PostMapping("/{groupId}/join-requests/{requestId}/approve")
    public JoinRequestResponse approveJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.approveJoinRequest(groupId, requestId, userDetails.getUsername());
    }

    @PostMapping("/{groupId}/join-requests/{requestId}/reject")
    public JoinRequestResponse rejectJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.rejectJoinRequest(groupId, requestId, userDetails.getUsername());
    }

    @PostMapping("/{groupId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupEventResponse createEvent(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.createEvent(groupId, request, userDetails.getUsername());
    }

    @GetMapping("/{groupId}/events")
    public List<GroupEventResponse> getEvents(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getEvents(groupId, userDetails.getUsername());
    }
}
