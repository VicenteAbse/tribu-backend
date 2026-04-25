package com.groupmatch.app.chat;

import com.groupmatch.app.domain.message.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    Page<MessageEntity> findByGroupIdOrderBySentAtAsc(Long groupId, Pageable pageable);
}
