package faang.school.postservice.dto.like;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeDto {

    private Long id;
    @NotNull
    @Positive
    private Long userId;
    @Positive
    private Long postId;
    @Positive
    private Long commentId;

}