package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.entity.PetGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 반려동물 프로필 작성 요청 DTO
 * - 클라이언트 → 서버로 전달되는 게시글 작성 정보
 * - 작성자는 Spring Security의 인증 정보에서 자동 추출
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPetProfileCreateRequestDto {
    private String name;
    private PetGender gender;
    private Integer age;
    private String preference;
    private String introduction;
    private String tel;

//    private ImageRequestDto image;
}
