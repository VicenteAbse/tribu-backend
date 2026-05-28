package com.groupmatch.app.chat;

import java.util.UUID;

public class ChatNotificationResponse {

    private UUID groupUuid;
    private String groupName;
    private String senderName;
    private String preview;

    public ChatNotificationResponse(UUID groupUuid, String groupName, String senderName, String preview) {
        this.groupUuid = groupUuid;
        this.groupName = groupName;
        this.senderName = senderName;
        this.preview = preview;
    }

    public UUID getGroupUuid() { return groupUuid; }
    public String getGroupName() { return groupName; }
    public String getSenderName() { return senderName; }
    public String getPreview() { return preview; }
}
