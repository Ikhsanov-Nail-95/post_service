package faang.school.postservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PostCreateRequest {

    private Long id;

    @NotBlank(message = "Post title cannot be empty")
    @Size(min = 1, max = 255, message = "Post title should be between 1 and 255 characters")
    private String title;

    @NotBlank(message = "Post content cannot be empty")
    @Size(min = 1, max = 4096, message = "Post content should be between 1 and 4096 characters")
    private String content;

    private Long authorId;
    private Long projectId;

    @FutureOrPresent(message = "Scheduled date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS Z")
    private ZonedDateTime scheduledAt;

}