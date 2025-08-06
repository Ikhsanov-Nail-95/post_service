package faang.school.postservice.dto.request;

import faang.school.postservice.model.enums.LikeTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class LikeRequest {

    @NotNull
    @Positive
    @Schema(description = "ID of the entity (post, comment, etc.) to which the like belongs", example = "1001")
    private Long entityId;

    @NotNull
    @Schema(description = "Type of entity to which the like belongs", example = "POST")
    private LikeTargetType targetType;

}