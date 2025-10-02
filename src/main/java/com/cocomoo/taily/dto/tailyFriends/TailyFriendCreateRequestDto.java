package com.cocomoo.taily.dto.tailyFriends;

import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TailyFriendCreateRequestDto {
    private String title;
    private String content;
    private String address;
<<<<<<< HEAD
    private Long userId;
=======
>>>>>>> develop
}

