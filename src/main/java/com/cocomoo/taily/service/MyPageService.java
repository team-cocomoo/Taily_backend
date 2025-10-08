package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.myPage.MyPetProfileCreateRequestDto;
import com.cocomoo.taily.dto.myPage.MypetProfileResponseDto;
import com.cocomoo.taily.entity.Pet;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.MyPetRepository;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MyPageService {
    public final MyPetRepository myPetRepository;
    public final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;

    @Transactional
    public MypetProfileResponseDto createMyPetProfile(MyPetProfileCreateRequestDto myPetProfileCreateRequestDto, String username) {
        log.info("=== 내 반려동물 프로필 작성 시작 : 주인={} ===", username);

        // 작성자 조회
        User petOwner = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        TableType tableType = tableTypeRepository.findById(2L).orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        // pet entity 생성
        Pet pet = Pet.builder()
                .name(myPetProfileCreateRequestDto.getName())
                .gender(myPetProfileCreateRequestDto.getGender())
                .preference(myPetProfileCreateRequestDto.getPreference())
                .introduction(myPetProfileCreateRequestDto.getIntroduction())
                .tel(myPetProfileCreateRequestDto.getTel())
                .user(petOwner)
                .build();

        Pet savedMyPetProfile = myPetRepository.save(pet);

        // 이미지 추후 추가

        log.info("내 반려동물 프로필 작성 완료: id={}, title={}", savedMyPetProfile.getId(), savedMyPetProfile.getName());

        return MypetProfileResponseDto.from(savedMyPetProfile);
    }
}
