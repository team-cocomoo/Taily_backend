package com.cocomoo.taily.dto.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRequestDto {

  @NotBlank(message = "제목은 필수 입력 항목입니다.")
  @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
  private String title;

  @NotBlank(message = "내용은 비워둘 수 없습니다.")
  private String content;
}
