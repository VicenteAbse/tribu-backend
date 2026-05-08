package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {

    List<GroupMemberEntity> findByGroupId(Long groupId);

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    @Query("SELECT m FROM GroupMemberEntity m WHERE m.user.id = :userId")
    List<GroupMemberEntity> findByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM GroupMemberEntity m WHERE m.group.id = :groupId AND m.user.id = :userId")
    Optional<GroupMemberEntity> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
