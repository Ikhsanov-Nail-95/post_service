package faang.school.postservice.controller;

import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.request.LikeRequest;
import faang.school.postservice.dto.response.LikeResponse;
import faang.school.postservice.model.enums.LikeTargetType;
import faang.school.postservice.service.like.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Like Controller", description = "Endpoints for liking and unliking posts/comments")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;
    private final UserContext userContext;

    @Operation(
            summary = "Add like a target entity (post, comment, etc.)",
            description = "Creates a like from the current user for a given target (post, comment, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Like successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "User not authorized"),
            @ApiResponse(responseCode = "404", description = "Target entity not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LikeResponse likeEntity(
            @RequestBody(required = true) @Valid @Parameter(description = "Like request data") LikeRequest likeRequest
    ) {
        long userId = userContext.getUserId();
        return likeService.likeEntity(userId, likeRequest);
    }

    @Operation(
            summary = "Remove a target entity (post, comment, etc.)",
            description = "Removes an existing like for the given target entity by the current user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Like successfully removed"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "User not authorized"),
            @ApiResponse(responseCode = "404", description = "Like or target not found")
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeEntity(
            @RequestBody @Valid @Parameter(description = "Like request data") LikeRequest likeRequest
    ) {
        long userId = userContext.getUserId();
        likeService.unlikeEntity(userId, likeRequest);
    }

    @Operation(
            summary = "Get users who liked a specific entity",
            description = "Returns a list of users who liked a specific entity, based on its ID and type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid entity type or ID"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/users")
    List<UserDto> getUsersWhoLikedEntity(
            @RequestParam
            @NotNull(message = "Like target type must not be null")
            @Parameter(description = "Type of liked entity (POST or COMMENT or etc.)", example = "POST")
            LikeTargetType targetType,

            @RequestParam
            @Min(value = 1, message = "Entity ID must be greater than 0")
            @Parameter(description = "ID of the liked entity (post, comment, etc.)", example = "123")
            long entityId
    ) {
        return likeService.getUsersWhoLikedEntity(targetType, entityId);
    }
}