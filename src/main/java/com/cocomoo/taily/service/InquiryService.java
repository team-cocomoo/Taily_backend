package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.inquiry.InquiryCreateRequestDto;
import com.cocomoo.taily.dto.inquiry.InquiryPageResponseDto;
import com.cocomoo.taily.dto.inquiry.InquiryResponseDto;
import com.cocomoo.taily.entity.Inquiry;
import com.cocomoo.taily.entity.InquiryState;
import com.cocomoo.taily.entity.InquiryType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.InquiryRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    // 문의, 답장 작성
    @Transactional
    public InquiryResponseDto createInquiry(InquiryCreateRequestDto dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Inquiry parent = null;
        InquiryType type;

        if (dto.getParentId() != null) {
            parent = inquiryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 문의를 찾을 수 없습니다."));
            type = InquiryType.REPLY;
            parent.updateState(InquiryState.RESOLVED);
        } else{
            type = InquiryType.ASK;
        }

        Inquiry inquiry = Inquiry.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(type)
                .state(dto.getParentId() == null ? InquiryState.PENDING : InquiryState.RESOLVED)
                .user(user)
                .parentInquiry(parent)
                .build();

        return InquiryResponseDto.from(inquiryRepository.save(inquiry));
    }
}
