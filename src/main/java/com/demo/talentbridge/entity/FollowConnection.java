package com.demo.talentbridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "follow_connections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followed_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    @Builder.Default
    private LocalDateTime followedAt = LocalDateTime.now();
}
