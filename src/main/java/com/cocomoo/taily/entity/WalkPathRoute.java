package com.cocomoo.taily.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false)
    private String updateAt;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "walk_paths_id", nullable = false)
//    private WalkPath walkPath;

}
