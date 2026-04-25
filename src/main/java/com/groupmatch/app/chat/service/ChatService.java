package com.groupmatch.app.chat.service;

import com.groupmatch.app.auth.UserRepository;
import com.groupmatch.app.chat.MessageRepository;
import com.groupmatch.app.chat.MessageResponse;
import com.groupmatch.app.chat.SendMessageRequest;
import com.groupmatch.app.domain.group.GroupEntity;
import com.groupmatch.app.domain.group.GroupStatus;
import com.groupmatch.app.domain.message.MessageEntity;
import com.groupmatch.app.domain.user.UserEntity;
import com.groupmatch.app.group.GroupMemberRepository;
import com.groupmatch.app.group.GroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public ChatService(MessageRepository messageRepository,
                       GroupRepository groupRepository,
                       GroupMemberRepository groupMemberRepository,
                       UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long groupId, String userEmail, Pageable pageable) {
        GroupEntity group = findActiveGroup(groupId);
        UserEntity user = findUserByEmail(userEmail);
        requireMembership(user.getId(), group.getId());
        return messageRepository.findByGroupIdOrderBySentAtAsc(groupId, pageable)
            .map(MessageResponse::new);
    }

    @Transactional
    public MessageResponse sendMessage(Long groupId, SendMessageRequest request, String userEmail) {
        GroupEntity group = findActiveGroup(groupId);
        UserEntity user = findUserByEmail(userEmail);
        requireMembership(user.getId(), group.getId());
        MessageEntity message = new MessageEntity(group, user, request.getContent());
        messageRepository.save(message);
        return new MessageResponse(message);
    }

    private GroupEntity findActiveGroup(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
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
