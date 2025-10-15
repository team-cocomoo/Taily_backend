package com.cocomoo.taily.repository;
import com.cocomoo.taily.entity.TailyFriend;
import com.cocomoo.taily.entity.WalkPath;
import com.cocomoo.taily.entity.WalkPathRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface WalkPathRoutesRepository extends JpaRepository<WalkPathRoute,Long> {
   List<WalkPathRoute> findByWalkPathId();

}
