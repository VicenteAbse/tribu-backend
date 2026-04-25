package com.groupmatch.app.group;

import com.groupmatch.app.group.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/groups")
    public List<GroupResponse> getGroups() {
        return groupService.getAll();
    }

    @PostMapping("/groups")
    public GroupResponse createGroup(@Valid @RequestBody GroupRequest request) {
        return groupService.create(request);
    }
}



