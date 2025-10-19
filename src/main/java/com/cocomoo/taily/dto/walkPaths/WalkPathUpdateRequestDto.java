// ✅ WalkPathUpdateRequestDto.java
package com.cocomoo.taily.dto.walkPaths;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkPathUpdateRequestDto {
    private Long postId;
    private String title;
    private String content;
    private List<WalkPathRouteRequestDto> routes;

//    private List<Long> deletedImageIds; // 삭제할 이미지 ID

}
