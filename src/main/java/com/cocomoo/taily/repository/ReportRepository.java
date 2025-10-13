package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
