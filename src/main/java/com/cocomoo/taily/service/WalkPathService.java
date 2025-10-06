package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.tailyFriends.TailyFriendDetailResponseDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathCreateRequestDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathDetailResponseDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathListResponseDto;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkPath;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.repository.WalkPathRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
@Slf4j
public class WalkPathService {
    private final WalkPathRepository walkPathRepository;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final UserService userService;

    //게시물 상세 조회
    @Transactional
    public WalkPathDetailResponseDto getWalkPathById(Long postId, String username) {
        log.info("게시글 상세 조회 : id = {}",postId);

        WalkPath post = walkPathRepository.findByIdWithUser(postId).orElseThrow(()->{
            log.error("게시글 조회 실패: id={}", postId);
            return new IllegalArgumentException("존재하지 않는 게시글입니다.");
        });

        // 현재 사용자의 좋아요 상태 확인
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TableType tableType = tableTypeRepository.findById(5L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        log.info("게시글 조회 성공: title={}", post.getTitle());

        return WalkPathDetailResponseDto.from(post);
    }

    //게시물 생성
    @Transactional
    public WalkPathDetailResponseDto createWalkPath(WalkPathCreateRequestDto walkPathCreateRequestDto){
        //작성자 조회
        User author = userRepository.findById(walkPathCreateRequestDto.getUserId()).orElseThrow(()
                -> new RuntimeException("회원을 찾을 수 없어 게시물을 등록할 수 없습니다 MEMBER ID:"
                + walkPathCreateRequestDto.getUserId()));
        //Post 생성
        WalkPath walkPath = WalkPath.builder()
                .title(walkPathCreateRequestDto.getTitle())
                .content(walkPathCreateRequestDto.getContent())
                //.user(author) // ManyToOne 관계 설정
                .build();
        //db에 저장
        WalkPath savedWalkPath = walkPathRepository.save(walkPath);
        return WalkPathDetailResponseDto.from(savedWalkPath);
    }

    //전체 게시물 목록으로 조회
    public List<WalkPathListResponseDto> findAllPostList() {

        List<WalkPath> posts = walkPathRepository.findAllWithUser(); // JPQL Fetch Join 적용된 repository 메서드 호출
        return posts.stream().map(WalkPathListResponseDto::from).collect(Collectors.toUnmodifiableList());
    }

    //

}
