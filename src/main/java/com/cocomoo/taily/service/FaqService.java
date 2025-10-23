package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.cs.FaqDetailResponseDto;
import com.cocomoo.taily.dto.cs.FaqPageResponseDto;
import com.cocomoo.taily.dto.cs.FaqRequestDto;
import com.cocomoo.taily.entity.Faq;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.FaqRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FaqService {
    private final FaqRepository faqRepository;
    private final UserRepository userRepository;

    /**
     * faq 전체 조회 + 페이징
     *
     * @param page
     * @param size
     * @return
     */
    public FaqPageResponseDto getFaqPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Faq> faqPage = faqRepository.findAll(pageable);

        List<FaqDetailResponseDto> faqList = faqPage.stream()
                .map(FaqDetailResponseDto::from)
                .toList();

        return FaqPageResponseDto.builder()
                .faqList(faqList)
                .totalCount(faqPage.getTotalElements())
                .build();
    }

    @Transactional
    public FaqDetailResponseDto createFaq(FaqRequestDto requestDto, String username) {
        log.info("=== faq 작성 시작 : username={} ===", username);
        // 관리자는 작성자 조회 필요 없음
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Faq faq = Faq.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .user(user)
                .build();

        Faq savedFaq = faqRepository.save(faq);

        log.info("faq 작성 완료: id={}, title={}, content={}", savedFaq.getId(), savedFaq.getTitle(), savedFaq.getContent());

        return FaqDetailResponseDto.from(savedFaq);
    }

    @Transactional
    public FaqDetailResponseDto updateFaq(Long id, FaqRequestDto faqRequestDto, String username) {
        Faq faq = faqRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("작성된 faq가 존재하지 않습니다."));

        if (!faq.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("관리자만 수정할 수 있습니다.");
        }

        faq.updateFaq(
                faqRequestDto.getTitle(),
                faqRequestDto.getContent()
        );
        return FaqDetailResponseDto.from(faq);
    }

    @Transactional
    public void deleteFaq(Long id, String username) {
        Faq faq = faqRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("작성된 faq가 존재하지 않습니다."));

        if (!faq.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("관리자만 삭제할 수 있습니다.");
        }
        faqRepository.delete(faq);
    }

    @Transactional
    public FaqDetailResponseDto getFaqById(Long id, String username) {
        User use = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        Faq faq = faqRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 faq 입니다."));

        return FaqDetailResponseDto.from(faq);
    }
}
