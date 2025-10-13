package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.TableType;
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

/*    @Query("SELECT i FROM Image i WHERE i.usersId = :user AND i.tableTypeId.id = 1")
    Optional<Image> findProfileImageByUser(@Param("user") User user);*/

    // 사용자 기준 프로필 이미지 조회
    Optional<Image> findByUserAndTableType_Id(User user, Long tableTypeId);

    // Feed 이미지
    @Query("SELECT i FROM Image i WHERE i.user.id = :userId AND i.tableType.id = 3")
    List<Image> findFeedImagesByUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Image i WHERE i.user.id = :userId AND i.tableType.id = 2")
    List<Image> findPetImagesByUserId(@Param("userId") Long userId);

    List<Image> findByTableTypeAndPostsId(TableType tableType, Long postsId);


}
