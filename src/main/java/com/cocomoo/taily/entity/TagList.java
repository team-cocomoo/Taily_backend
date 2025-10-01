package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tag_lists")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feeds_id", nullable = false, foreignKey = @ForeignKey(name="fk_tag_lists_feeds_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tags_id", nullable = false, foreignKey = @ForeignKey(name="fk_tags_list_tags_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tag tag;
}