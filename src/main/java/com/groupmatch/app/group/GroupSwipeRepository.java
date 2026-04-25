package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupSwipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupSwipeRepository extends JpaRepository<GroupSwipeEntity, Long> {

    Optional<GroupSwipeEntity> findByUserIdAndGroupId(Long userId, Long groupId);

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    @Query("SELECT s FROM GroupSwipeEntity s WHERE s.group.id = :groupId AND s.liked = true")
    List<GroupSwipeEntity> findLikersByGroupId(@Param("groupId") Long groupId);
}
