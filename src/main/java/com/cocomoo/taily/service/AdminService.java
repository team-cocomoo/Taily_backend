package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.admin.UserListResponseDto;
import com.cocomoo.taily.dto.admin.UserPageResponseDto;
import com.cocomoo.taily.entity.User;
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
public class AdminService {
    private final UserRepository userRepository;

    /**
     * 전체 회원 리스트 + 검색 + 페이지네이션
     * GET http://localhost:8080/api/admin?keyword=taily&page=1&size=5
     *
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    public UserPageResponseDto getUsersPage(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAndSearchUser(keyword, pageable);

        List<UserListResponseDto> userList = usersPage.stream()
                .map(UserListResponseDto::from)
                .toList();
        return UserPageResponseDto.builder()
                .data(userList)
                .totalCount(usersPage.getTotalElements())
                .build();
    }
}
