package com.cocomoo.taily.repository;


import com.cocomoo.taily.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Optional<Inquiry> findByParentInquiryId(Long parentId);
}
