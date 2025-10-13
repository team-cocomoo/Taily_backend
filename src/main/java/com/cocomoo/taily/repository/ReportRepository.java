package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Report;
import com.cocomoo.taily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterAndReportedAndPath(User reporterId, User reportedId, String path);
}
