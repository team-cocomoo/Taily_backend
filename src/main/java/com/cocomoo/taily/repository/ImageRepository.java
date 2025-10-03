package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image,Long> {
    List<Image> findByPostsId(Long id);
}
