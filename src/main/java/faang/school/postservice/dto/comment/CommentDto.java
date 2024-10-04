package faang.school.postservice.dto.comment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {

    private Long id;
    @NotNull
    private Long authorId;
    @Size(min = 1, max = 4096, message = "Comment should be between 1 and 4096 symbols")
    private String content;
    private List<Long> likesIds;
    @NotNull
    private Long postId;

}