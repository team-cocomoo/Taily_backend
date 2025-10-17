package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.inquiry.InquiryCreateRequestDto;
import com.cocomoo.taily.dto.inquiry.InquiryResponseDto;
import com.cocomoo.taily.entity.Inquiry;
import com.cocomoo.taily.entity.InquiryType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.InquiryRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        } else{
            type = InquiryType.ASK;
        }

        Inquiry inquiry = Inquiry.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(type)
                .user(user)
                .parentInquiry(parent)
                .build();

        return InquiryResponseDto.from(inquiryRepository.save(inquiry));
    }

    // 모든 문의 조회
    public List<InquiryResponseDto> getAllInquiries() {
        return inquiryRepository.findAll().stream()
                .map(InquiryResponseDto::from)
                .collect(Collectors.toList());
    }

    // 특정 문의 조회
    public InquiryResponseDto getInquiry(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의가 존재하지 않습니다."));
        return InquiryResponseDto.from(inquiry);
    }

    // 특정 답변 조회
    public InquiryResponseDto getReply(Long parentId) {
        Inquiry reply = inquiryRepository.findByParentInquiryId(parentId)
                .orElseThrow(() -> new IllegalArgumentException("답변이 존재하지 않습니다."));
        return InquiryResponseDto.from(reply);
    }

    // 문의 삭제
    @Transactional
    public void deleteInquiry(Long id) {
        inquiryRepository.deleteById(id);
    }

}
