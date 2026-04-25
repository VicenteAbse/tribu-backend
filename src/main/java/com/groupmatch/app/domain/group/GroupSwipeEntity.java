package com.groupmatch.app.domain.group;

import com.groupmatch.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "group_swipes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"})
)
public class GroupSwipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(nullable = false)
    private boolean liked;

    @Column(nullable = false, updatable = false)
    private LocalDateTime swipedAt;

    protected GroupSwipeEntity() {}

    public GroupSwipeEntity(UserEntity user, GroupEntity group, boolean liked) {
        this.user = user;
        this.group = group;
        this.liked = liked;
        this.swipedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public GroupEntity getGroup() { return group; }
    public boolean isLiked() { return liked; }
    public LocalDateTime getSwipedAt() { return swipedAt; }
}
