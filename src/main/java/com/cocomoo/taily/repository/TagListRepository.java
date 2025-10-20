package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Feed;
import com.cocomoo.taily.entity.Tag;
import com.cocomoo.taily.entity.TagList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagListRepository extends JpaRepository<TagList, Long> {
    boolean existsByFeedAndTag(Feed feed, Tag tag);
    List<TagList> findByFeed(Feed feed);
}
