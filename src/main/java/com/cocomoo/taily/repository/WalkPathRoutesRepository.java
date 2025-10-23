package com.cocomoo.taily.repository;
import com.cocomoo.taily.entity.WalkPathRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WalkPathRoutesRepository extends JpaRepository<WalkPathRoute,Long> {
    //산책경로 게시물 중 특정 게시물의 산책경로 지점들을 list 형태로 가져옴
    @Query("SELECT r FROM WalkPathRoute r " +
            "JOIN r.walkPath w " +
            "WHERE w.id = :walkPathId " +
            "ORDER BY r.orderNo ASC")
    List<WalkPathRoute> findAllByWalkPathId(@Param("walkPathId") Long walkPathId);

    // 특정 게시글(post)의 모든 경로를 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM WalkPathRoute r WHERE r.walkPath.id = :walkPathId")
    void deleteByWalkPath_Id(@Param("walkPathId") Long walkPathId);

}
