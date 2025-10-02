package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Like 엔티티
 */
@Entity
@Table(name = "likes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean state;
    @Column(name = "posts_id", nullable = false)
    private Long postsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User usersId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false)
    private TableType tableTypesId;

    public void toggle() {
        this.state = !this.state;
    }
}
