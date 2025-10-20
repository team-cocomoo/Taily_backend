package com.cocomoo.taily.dto.admin;

import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSummaryDto {
    private String publicId;
    private String nickname;

    public static AdminSummaryDto fromEntity(User user) {
        if (user == null) return null;
        return AdminSummaryDto.builder()
                .publicId(user.getPublicId())
                .nickname(user.getNickname())
                .build();
    }
}
