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

    // 특정 사용자(User) 기준 모든 이미지 조회
    List<Image> findByUser(User user);

    // 특정 사용자(User) 기준, 가장 최신의 단일 이미지 조회 (예: 프로필 이미지)
    Optional<Image> findFirstByUserOrderByCreatedAtDesc(User user);

    // 특정 사용자의 특정 기능에 해당하는 모든 이미지 목록
    @Query("SELECT i FROM Image i WHERE i.user.id = :userId AND i.tableTypesId = :tableTypesId")
    // i.user.id : Image 엔티티의 user 필드안 id 속성 호출
    // i.tableTypesId : Image 엔티티의 tableTypesId 필드 호출
    List<Image> findByUserIdAndTableTypesId(@Param("userId") Long userId, @Param("tableTypesId") Long tableTypesId);
    // user.id를 사용하기 위해서 JPQL로 작성

    // 프로필용 최신 1개 이미지만 조회
    Optional<Image> findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(Long userId, Long tableTypesId);

    // 특정 게시글(postsId)와 테이블 타입(tableTypesId)에 해당하는 모든 이미지 조회
    List<Image> findByPostsIdAndTableTypesId(Long postsId, Long tableTypesId);

    // 특정 게시글과 테이블 타입에 해당하는 단일 이미지 조회 (Optional)
    Optional<Image> findFirstByPostsIdAndTableTypesId(Long postsId, Long tableTypesId);

    List<Image> findAllByTableTypesId(long l);
}