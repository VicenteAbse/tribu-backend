package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.JoinRequestEntity;
import com.groupmatch.app.domain.group.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequestEntity, Long> {

    @Query("SELECT r FROM JoinRequestEntity r WHERE r.group.id = :groupId AND r.status = :status")
    List<JoinRequestEntity> findByGroupIdAndStatus(@Param("groupId") Long groupId,
                                                    @Param("status") JoinRequestStatus status);

    @Query("SELECT r FROM JoinRequestEntity r WHERE r.group.id = :groupId AND r.id = :requestId")
    Optional<JoinRequestEntity> findByGroupIdAndId(@Param("groupId") Long groupId,
                                                    @Param("requestId") Long requestId);

    boolean existsByUserIdAndGroupIdAndStatus(Long userId, Long groupId, JoinRequestStatus status);

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
}
