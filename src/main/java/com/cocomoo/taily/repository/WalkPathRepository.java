package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.WalkPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalkPathRepository extends JpaRepository<WalkPath,Long>{
    @Query("SELECT wp FROM WalkPath wp JOIN FETCH wp.user")
    List<WalkPath> findAllWithUser();

}
