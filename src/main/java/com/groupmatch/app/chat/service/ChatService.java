package com.groupmatch.app.chat.service;

import com.groupmatch.app.auth.UserRepository;
import com.groupmatch.app.chat.ChatNotificationResponse;
import com.groupmatch.app.chat.MessageRepository;
import com.groupmatch.app.chat.MessageResponse;
import com.groupmatch.app.chat.SendMessageRequest;
import com.groupmatch.app.domain.group.GroupEntity;
import com.groupmatch.app.domain.group.GroupMemberEntity;
import com.groupmatch.app.domain.group.GroupStatus;
import com.groupmatch.app.domain.message.MessageEntity;
import com.groupmatch.app.domain.user.UserEntity;
import com.groupmatch.app.group.GroupMemberRepository;
import com.groupmatch.app.group.GroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(MessageRepository messageRepository,
                       GroupRepository groupRepository,
                       GroupMemberRepository groupMemberRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID groupUuid, String userEmail, Pageable pageable) {
        GroupEntity group = findActiveGroup(groupUuid);
        UserEntity user = findUserByEmail(userEmail);
        requireMembership(user.getId(), group.getId());
        return messageRepository.findByGroupIdOrderBySentAtAsc(group.getId(), pageable)
            .map(MessageResponse::new);
    }

    @Transactional
    public MessageResponse sendMessage(UUID groupUuid, SendMessageRequest request, String userEmail) {
        GroupEntity group = findActiveGroup(groupUuid);
        UserEntity user = findUserByEmail(userEmail);
        requireMembership(user.getId(), group.getId());

        MessageEntity message = new MessageEntity(group, user, request.getContent());
        messageRepository.save(message);

        MessageResponse response = new MessageResponse(message);

        // broadcast al topic del grupo
        messagingTemplate.convertAndSend("/topic/group/" + group.getUuid(), response);

        // notificación ligera a cada miembro (excepto el remitente)
        String preview = request.getContent().length() > 60
                ? request.getContent().substring(0, 60) + "..."
                : request.getContent();
        ChatNotificationResponse notification = new ChatNotificationResponse(
                group.getUuid(), group.getName(),
                user.getName() != null ? user.getName() : user.getEmail(),
                preview
        );
        List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(group.getId());
        for (GroupMemberEntity member : members) {
            if (!member.getUser().getId().equals(user.getId())) {
                messagingTemplate.convertAndSend(
                        "/topic/user/" + member.getUser().getUuid() + "/notifications",
                        notification
                );
            }
        }

        return response;
    }

    private GroupEntity findActiveGroup(UUID groupUuid) {
        GroupEntity group = groupRepository.findByUuid(groupUuid)
            .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        if (group.getStatus() != GroupStatus.ACTIVE) {
            throw new IllegalStateException("El chat solo está disponible cuando el grupo está activo");
        }
        return group;
    }

    private void requireMembership(Long userId, Long groupId) {
        if (!groupMemberRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new IllegalStateException("No eres miembro de este grupo");
        }
    }

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}
