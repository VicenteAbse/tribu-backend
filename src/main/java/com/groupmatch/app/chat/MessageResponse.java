package com.groupmatch.app.chat;

import com.groupmatch.app.domain.message.MessageEntity;

import java.time.LocalDateTime;

public class MessageResponse {

    private Long id;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;

    public MessageResponse(MessageEntity message) {
        this.id = message.getId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getName() != null
            ? message.getSender().getName()
            : message.getSender().getEmail();
        this.content = message.getContent();
        this.sentAt = message.getSentAt();
    }

    public Long getId() { return id; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
}
