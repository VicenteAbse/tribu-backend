package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupEntity;
import com.groupmatch.app.domain.group.GroupStatus;
import com.groupmatch.app.domain.user.Gender;
import com.groupmatch.app.domain.group.GenderPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("""
        SELECT g FROM GroupEntity g
        WHERE g.status = :status
          AND g.creator.id <> :userId
          AND g.id NOT IN (
              SELECT s.group.id FROM GroupSwipeEntity s WHERE s.user.id = :userId
          )
          AND (
              g.genderPreference = 'MIXED'
              OR (g.genderPreference = 'MEN_ONLY' AND :gender = 'MALE')
              OR (g.genderPreference = 'WOMEN_ONLY' AND :gender = 'FEMALE')
          )
        """)
    Page<GroupEntity> findDiscoverableGroups(
        @Param("userId") Long userId,
        @Param("status") GroupStatus status,
        @Param("gender") String gender,
        Pageable pageable
    );
}
