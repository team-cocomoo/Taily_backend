package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag_lists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feeds_id", nullable = false)
    private Long feedId;

    @Column(name = "tags_id", nullable = false)
    private Long tagId;
}