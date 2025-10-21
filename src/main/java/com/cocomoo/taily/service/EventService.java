package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.cs.EventDetailResponseDto;
import com.cocomoo.taily.dto.cs.EventListResponseDto;
import com.cocomoo.taily.dto.cs.EventPageResponseDto;
import com.cocomoo.taily.entity.Event;
import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.EventRepository;
import com.cocomoo.taily.repository.ImageRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    /**
     * 이벤트 상세 조회
     */
    @Transactional(readOnly = true)
    public EventDetailResponseDto getEventDetail(Long id,String username){

        User use = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        //이벤트 데이터 조회
        Event event = eventRepository.findByIdWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다. id=" + id));

        //단일 이미지 경로 조회
        Optional<Image> imageOpt = imageRepository.findFirstByPostsIdAndTableTypesId(id, 7L);

        //Optional 처리 → URL 리스트 형태로 맞춤
        List<String> imageUrls = imageOpt
                .map(image -> Collections.singletonList(image.getFilePath()))
                .orElse(Collections.emptyList());

        //DTO 변환
        return EventDetailResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .view(event.getView())
                .username(event.getUser().getUsername())
                .createdAt(event.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
    /**
     * 이벤트 전체 목록 조회
     */
    public EventPageResponseDto getEventPage(int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        Page<Event> eventPage = eventRepository.findAll(pageable);

        List<EventListResponseDto> eventList = eventPage.stream()
                .map(event -> {
                    Optional<Image> imageOpt = imageRepository.findFirstByPostsIdAndTableTypesId(event.getId(), 7L);
                    String imageUrl = imageOpt.map(Image::getFilePath).orElse(null);

                    return EventListResponseDto.builder()
                            .id(event.getId())
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList();
        return EventPageResponseDto.builder()
                .eventList(eventList)
                .totalCount(eventPage.getTotalElements())
                .build();
    }

}
