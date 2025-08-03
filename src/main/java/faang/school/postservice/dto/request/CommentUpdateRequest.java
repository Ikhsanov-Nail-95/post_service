package faang.school.postservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CommentUpdateRequest {

    @NotBlank(message = "Comment content cannot be empty")
    @Size(min = 1, max = 4096, message = "Comment content should be between 1 and 4096 characters")
    private String content;

}