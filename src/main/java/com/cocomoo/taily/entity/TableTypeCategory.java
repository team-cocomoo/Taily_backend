package com.cocomoo.taily.entity;

import lombok.Getter;

@Getter
public enum TableTypeCategory {
    USERS("회원"),
    PETS("반려동물"),
    FEEDS("피드"),
    WALK_DIARIES("산책일지"),
    TAILY_FRIENDS("테일리프렌즈"),
    WALK_PATHS("산책경로"),
    EVENTS("이벤트");

    private final String displayName;   // 한글명 저장
    TableTypeCategory(String displayName) {
        this.displayName = displayName;
    }
}
