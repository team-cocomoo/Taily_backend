package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image,Long> {
    List<Image> findByPostsId(Long id);

    @Query("SELECT i FROM Image i WHERE i.usersId = :user AND i.tableTypeId.id = 1")
    Optional<Image> findProfileImageByUser(@Param("user") User user);

    Optional<Image> findTopByUsersId_IdAndTableTypeId_Id(Long userId, Long tableTypeId);
}
