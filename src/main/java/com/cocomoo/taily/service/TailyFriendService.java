package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.tailyFriends.TailyFriendCreateRequestDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendDetailResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendListResponseDto;
import com.cocomoo.taily.entity.Like;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.TailyFriend;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.LikeRepository;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.TailyFriendRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Member;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TailyFriendService {
    private final TailyFriendRepository tailyFriendRepository;
    private final UserService userService;
    private final TableTypeRepository tableTypeRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    // 테일리 프렌즈 작성
   @Transactional
    public TailyFriendDetailResponseDto createTailyFriend(TailyFriendCreateRequestDto requestDto, String username){
    log.info("게시글 작성: username = {}",username);

       User author = userService.getUserEntity(username);

       TailyFriend post = TailyFriend.builder()
               .title(requestDto.getTitle())
               .address(requestDto.getAddress())
               .content(requestDto.getContent())
               .user(author)
               .build();
       TailyFriend savedPost = tailyFriendRepository.save(post);
       log.info("게시글 작성 완료 id = {}, title = {}",savedPost.getId(),savedPost.getTitle());

       return TailyFriendDetailResponseDto.from(savedPost,false);
   }

   // 테일리 프렌즈 상세 조회
    @Transactional
   public TailyFriendDetailResponseDto getTailyFriendById(Long postId, String username){
       log.info("게시글 상세 조회 : id = {}",postId);

       TailyFriend post = tailyFriendRepository.findByIdWithUser(postId).orElseThrow(()->{
           log.error("게시글 조회 실패: id={}", postId);
           return new IllegalArgumentException("존재하지 않는 게시글입니다.");
       });

       // 조회수 증가
       post.increaseView();

       // 현재 사용자의 좋아요 상태 확인
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TableType tableType = tableTypeRepository.findById(5L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        boolean liked = likeRepository.existsByPostsIdAndTableTypesIdAndUsersIdAndState(
                post.getId(), tableType, user, true
        );

       log.info("게시글 조회 성공: title={}", post.getTitle());

       return TailyFriendDetailResponseDto.from(post,liked);
   }

   // 테일리 프렌즈 전체 조회
    public List<TailyFriendListResponseDto> getAllTailyFriends(){
        List<TailyFriend> posts = tailyFriendRepository.findAllWithUser();
        log.info("조회된 게시글 수 : {}", posts.size());
        return posts.stream().map(TailyFriendListResponseDto::from).collect(Collectors.toList());
    }

    // 좋아요 상태 변화
    @Transactional
    public void toggleLike(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TailyFriend post = tailyFriendRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        TableType tableType = tableTypeRepository.findById(5L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        Like like = likeRepository.findByPostsIdAndTableTypesIdAndUsersIdAndState(
                post.getId(), tableType, user, true
        ).orElse(null);

        if (like == null) {
            // 좋아요 생성
            likeRepository.save(
                    Like.builder()
                            .postsId(post.getId())
                            .usersId(user)
                            .tableTypesId(tableType)
                            .state(true)
                            .build()
            );
            post.increaseLike();
        } else {
            // 좋아요 취소
            like.toggle();
            post.decreaseLike();
        }
    }
}
