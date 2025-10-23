package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Report;
import com.cocomoo.taily.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterAndReportedAndPath(User reporterId, User reportedId, String path);

    @Query("""
            SELECT r FROM Report r
            WHERE (:keyword IS NULL
               OR r.reported.nickname LIKE CONCAT('%', :keyword, '%')
               OR r.reporter.nickname LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<Report> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Report r")
    Page<Report> findAllWithPage(Pageable pageable);
}
