package com.groupmatch.app.group.service;

import com.groupmatch.app.group.GroupRequest;
import com.groupmatch.app.group.GroupResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    private final List<GroupResponse> groups = new ArrayList<>();

    public List<GroupResponse> getAll() {
        return groups;
    }

    public GroupResponse create(GroupRequest request) {
        GroupResponse newGroup = new GroupResponse(
                (long) (groups.size() + 1),
                request.getName(),
                request.getDescription()
        );

        groups.add(newGroup);
        return newGroup;
    }
}

