package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupEventRepository extends JpaRepository<GroupEventEntity, Long> {

    List<GroupEventEntity> findByGroupIdOrderByEventDateAsc(Long groupId);
}
