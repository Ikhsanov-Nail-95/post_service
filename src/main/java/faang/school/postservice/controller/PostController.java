package faang.school.postservice.controller;

import faang.school.postservice.dto.request.PostCreateRequest;
import faang.school.postservice.dto.response.PostResponse;
import faang.school.postservice.dto.request.PostUpdateRequest;
import faang.school.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Post Controller",
        description = "Endpoints for managing posts: create drafts, publish, update, delete, and retrieve posts by ID, title, author or project"
)
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "Creating a draft post",
            description = "Each post must have exactly one author — either a user or a project"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Draft post created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@RequestBody @Valid PostCreateRequest postCreateRequest) {
        return postService.createPost(postCreateRequest);
    }

    @Operation(
            summary = "Publish a draft post by ID",
            description = "A post cannot be published more than once"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post published"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "409", description = "Already published")
    })
    @PatchMapping("/{id}/publish")
    public PostResponse publishPost(@PathVariable("id") @Positive(message = "Post ID must be positive") long id) {
        return postService.publishPost(id);
    }

    @Operation(
            summary = "Update a post by ID",
            description = "The author cannot be changed or removed"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden to modify author"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PutMapping("/{id}")
    public PostResponse updatePost(
            @PathVariable("id") @Positive(message = "Post ID must be positive") long id,
            @RequestBody @Valid PostUpdateRequest postUpdateRequest
    ) {
        return postService.updatePost(id, postUpdateRequest);
    }

    @Operation(
            summary = "Delete a post by ID",
            description = "Marks the post as deleted without removing it from the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post deleted"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "409", description = "Post already deleted")
    })
    @DeleteMapping("/{id}")
    public PostResponse deletePost(@PathVariable("id") @Positive(message = "Post ID must be positive") long id) {
        return postService.deletePost(id);
    }

    @Operation(summary = "Get a post by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post received"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable("id") @Positive(message = "Post ID must be positive") long id) {
        return postService.getPostById(id);
    }

    @Operation(summary = "Get posts by title part (case-insensitive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found")
    })
    @GetMapping("/search")
    public List<PostResponse> getPostByTitle(
            @RequestParam("titlePart") @NotEmpty(message = "Post title must not be empty") String titlePart) {
        return postService.getPostByTitle(titlePart);
    }

    @Operation(
            summary = "Get all draft posts by user ID",
            description = "Returns all non-deleted draft posts authored by the specified user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Drafts post received"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/author/{authorId}/drafts")
    public List<PostResponse> getDraftsByAuthorId(
            @PathVariable("authorId")
            @Positive(message = "User ID must be positive") long authorId
    ) {
        return postService.getDraftsByAuthorId(authorId);
    }

    @Operation(
            summary = "Get all draft posts by project ID",
            description = "Returns all non-deleted draft posts authored by the specified project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Drafts post received"),
            @ApiResponse(responseCode = "400", description = "Invalid project ID format"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/project/{projectId}/drafts")
    public List<PostResponse> getDraftsByProjectId(
            @PathVariable("projectId")
            @Positive(message = "Project ID must be positive") long projectId
    ) {
        return postService.getDraftsByProjectId(projectId);
    }

    @Operation(
            summary = "Get all published posts by user ID",
            description = "Returns all published non-deleted posts authored by the specified user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Published post received"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/author/{authorId}")
    public List<PostResponse> getPostsByAuthorId(
            @PathVariable("authorId")
            @Positive(message = "User ID must be positive") long authorId
    ) {
        return postService.getPostsByAuthorId(authorId);
    }

    @Operation(
            summary = "Get all published posts by project ID",
            description = "Returns all published non-deleted posts authored by the specified project"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Published post received"),
            @ApiResponse(responseCode = "400", description = "Invalid project ID format"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/project/{projectId}")
    public List<PostResponse> getPostsByProjectId(
            @PathVariable("projectId")
            @Positive(message = "Project ID must be positive") long projectId
    ) {
        return postService.getPostsByProjectId(projectId);
    }

}