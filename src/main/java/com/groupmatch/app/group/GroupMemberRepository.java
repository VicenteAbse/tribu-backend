package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {

    List<GroupMemberEntity> findByGroupId(Long groupId);

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    @Query("SELECT m FROM GroupMemberEntity m WHERE m.user.id = :userId")
    List<GroupMemberEntity> findByUserId(@Param("userId") Long userId);
}
