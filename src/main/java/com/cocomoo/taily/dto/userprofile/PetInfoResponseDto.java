package com.cocomoo.taily.dto.userprofile;

import com.cocomoo.taily.entity.PetGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetInfoResponseDto {
    private String name;
    private PetGender gender;
    private String preference;
    private String introduction;
    private LocalDateTime createdAt;
}
