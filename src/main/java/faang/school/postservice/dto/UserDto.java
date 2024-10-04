package faang.school.postservice.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
}