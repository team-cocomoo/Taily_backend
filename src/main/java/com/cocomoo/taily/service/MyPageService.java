package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.myPage.MyPetProfileCreateRequestDto;
import com.cocomoo.taily.dto.myPage.MyPetProfileResponseDto;
import com.cocomoo.taily.dto.myPage.MyPetProfileUpdateRequestDto;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MyPageService {
    public final MyPetRepository myPetRepository;
    public final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;

    @Transactional
    public MyPetProfileResponseDto createMyPetProfile(MyPetProfileCreateRequestDto myPetProfileCreateRequestDto, String username) {
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

        return MyPetProfileResponseDto.from(savedMyPetProfile);
    }
    @Transactional
    public List<MyPetProfileResponseDto> getMyPetProfiles(String username) {
        log.info("=== 내 반려동물 프로필 리스트 조회 시작 ===");

        List<Pet> myPetProfiles = myPetRepository.findMyPetProfilesByPetOwner(username);

        log.info("조회된 산책 내 반려동물 프로필 리스트 수 : {}", myPetProfiles.size());

        return myPetProfiles.stream().map(MyPetProfileResponseDto::from).collect(Collectors.toList());
    }
    @Transactional
    public MyPetProfileResponseDto updateMyPetProfile(Long id, MyPetProfileUpdateRequestDto myPetProfileUpdateRequestDto, String username) {
        Pet pet = myPetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("작성된 내 반려동물 프로필이 존재하지 않습니다."));

        if (!pet.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인 반려동물 프로필만 수정할 수 있습니다.");
        }

        pet.updateMyPetProfile(
                myPetProfileUpdateRequestDto.getName(),
                myPetProfileUpdateRequestDto.getGender(),
                myPetProfileUpdateRequestDto.getPreference(),
                myPetProfileUpdateRequestDto.getIntroduction()
        );

        // 이미지 수정

        return MyPetProfileResponseDto.from(pet);
    }

    @Transactional
    public void deleteMyPetProfile(Long id, String username) {
        Pet pet = myPetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("내 반려동물 프로필이 존재하지 않습니다."));

        if (!pet.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인 반려동물 프로필만 삭제할 수 있습니다.");
        }
        myPetRepository.delete(pet);
    }
}
