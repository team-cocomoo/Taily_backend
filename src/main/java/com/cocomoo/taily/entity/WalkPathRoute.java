package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "walk_paths_routes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WalkPathRoute {
//    	`id`	BIGINT	NOT NULL,
//            `address`	VARCHAR(50)	NOT NULL,
//	`order_no`	INT	NOT NULL,
//            `created_at`	DATETIME	NOT NULL,
//            `updated_at`	DATETIME	NOT NULL,
//            `walk_paths_id`	BIGINT	NOT NULL

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String address;

    @Column(name = "order_no", nullable = false)
    private Integer orderNo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private String createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private String updateAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "walk_paths_id", nullable = false)
    private WalkPath walkPath;

}
