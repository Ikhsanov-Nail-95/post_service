package faang.school.postservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import faang.school.postservice.model.enums.LikeTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LikeResponse {

    @Schema(description = "Like ID", example = "123")
    private long id;

    @Schema(description = "User ID", example = "42")
    private long userId;

    @Schema(description = "ID сущности", example = "1001")
    private long entityId;

    @Schema(description = "Type of entity", example = "COMMENT")
    private LikeTargetType targetType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date and time of the like creation", example = "2025-07-15T10:30:00")
    private ZonedDateTime createdAt;

}