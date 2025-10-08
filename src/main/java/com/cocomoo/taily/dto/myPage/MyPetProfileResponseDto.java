package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.entity.Pet;
import com.cocomoo.taily.entity.PetGender;
import com.cocomoo.taily.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 내 반려동물 상세 응답 DTO
 * - 내 반려동물 상세 조회 시 모든 정보 포함
 * - 작성자 정보도 함께 제공
 * - 수정/삭제 권한 체크를 위한 username 포함
 */
@Getter
@Builder
public class MyPetProfileResponseDto {
    private Long petId;
    private String name;
    private PetGender gender;
    private String preference;
    private String introduction;
    private String tel;
    private Long userId;
    private String username;
    private String userNickname;
    private LocalDateTime createdAt;
    //    private ImageResponseDto image;

    //public static MypetProfileResponseDto from (Pet pet, ImageResponseDto image) {
    public static MyPetProfileResponseDto from (Pet pet) {
        User user = pet.getUser();

        return MyPetProfileResponseDto.builder()
                .petId(pet.getId())
                .name(pet.getName())
                .gender(pet.getGender())
                .preference(pet.getPreference())
                .introduction(pet.getIntroduction())
                .tel(pet.getTel())
                .userId(user.getId())
                .username(user.getUsername())
                .userNickname(user.getNickname())
                .createdAt(pet.getCreatedAt())
//                .image(image)
                .build();
    }
}
