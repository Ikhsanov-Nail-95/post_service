package faang.school.postservice.controller;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.request.CommentCreateRequest;
import faang.school.postservice.dto.request.CommentUpdateRequest;
import faang.school.postservice.dto.response.CommentResponse;
import faang.school.postservice.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment Controller", description = "Endpoints for managing comments: create, update, delete, and get comments by post ID")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserContext userContext;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(@RequestBody @Valid CommentCreateRequest commentCreateRequest) {
        long userId = userContext.getUserId();
        return commentService.createComment(userId, commentCreateRequest);
    }

    @PutMapping("/{id}")
    public CommentResponse updateComment(
            @PathVariable("id") @Positive(message = "Comment ID must be positive") long id,
            @RequestBody @Valid CommentUpdateRequest commentUpdateRequest
    ) {
        return commentService.updateComment(id, commentUpdateRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable("id") @Positive(message = "Comment ID must be positive") long id) {
        commentService.deleteComment(id);
    }

    @GetMapping("/by-post/{postId}")
    public Page<CommentResponse> getCommentsByPostId(
            @PathVariable("postId") @Positive(message = "Post ID must be positive") long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.getCommentsByPostId(postId, page, size);
    }

}