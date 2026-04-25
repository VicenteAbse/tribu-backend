package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupStatus;

public class SwipeResponse {

    private boolean liked;
    private GroupStatus groupStatus;
    private boolean groupActivated;
    private Integer likesLeftToday;

    public SwipeResponse(boolean liked, GroupStatus groupStatus, boolean groupActivated, Integer likesLeftToday) {
        this.liked = liked;
        this.groupStatus = groupStatus;
        this.groupActivated = groupActivated;
        this.likesLeftToday = likesLeftToday;
    }

    public boolean isLiked() { return liked; }
    public GroupStatus getGroupStatus() { return groupStatus; }
    public boolean isGroupActivated() { return groupActivated; }
    public Integer getLikesLeftToday() { return likesLeftToday; }
}
