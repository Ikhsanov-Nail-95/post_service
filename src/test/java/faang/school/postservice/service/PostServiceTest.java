package faang.school.postservice.service;

import faang.school.postservice.dto.request.PostCreateRequest;
import faang.school.postservice.dto.request.PostUpdateRequest;
import faang.school.postservice.dto.response.PostResponse;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.helper.PostAnalyticsEventHelper;
import faang.school.postservice.mapper.PostMapperImpl;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.validator.PostValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private AuthorValidationService authorValidationService;
    @Mock private PostValidator postValidator;
    @Mock private PostRepository postRepository;
    @Mock private PostAnalyticsEventHelper postAnalyticsEventHelper;

    @Spy private PostMapperImpl postMapper = new PostMapperImpl();

    @InjectMocks private PostService postService;

    private long postId;
    private long userIdOrProjectId;
    private long viewerUserId;
    private PostResponse postResponse;
    private Post post;
    List<PostResponse> listPostResponse;

    @BeforeEach
    void setUp() {
        postId = 0L;
        userIdOrProjectId = 1L;
        viewerUserId = 2L;

        post = Post.builder()
                .id(postId)
                .title("Test title")
                .content("Test content")
                .authorId(userIdOrProjectId)
                .verified(true)
                .build();

        postResponse = null;

        listPostResponse = new ArrayList<>();
    }

    @Test
    @DisplayName("Should create and return post when input is valid")
    void createPost_shouldReturnPost_whenValid() {
        PostCreateRequest postCreateRequest = new PostCreateRequest();
        postCreateRequest.setTitle("Test title");
        postCreateRequest.setContent("Test content");
        postCreateRequest.setAuthorId(userIdOrProjectId);

        when(postRepository.save(any(Post.class))).thenReturn(post);

        postResponse = postService.createPost(postCreateRequest);

        assertEquals("Test title", postResponse.getTitle());
        assertEquals("Test content", postResponse.getContent());
        assertEquals(userIdOrProjectId, postResponse.getAuthorId());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository, times(1)).save(captor.capture());

        Post capturedPost = captor.getValue();
        assertEquals(postId, capturedPost.getId());
        assertEquals("Test title", capturedPost.getTitle());
        assertEquals("Test content", capturedPost.getContent());
        assertEquals(userIdOrProjectId, capturedPost.getAuthorId());
        assertTrue(capturedPost.getVerified());

        verify(postValidator, times(1)).validateAuthor(postCreateRequest);
        verify(postMapper, times(1)).toEntity(postCreateRequest);
        verify(postRepository, times(1)).save(any(Post.class));
        verify(postMapper, times(1)).toResponse(post);
    }

    @Test
    @DisplayName("Should publish post when it exists and is valid")
    void publishPost_shouldPublishPost_whenValid() {
        post.setDeleted(false);
        post.setPublished(false);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postResponse = postService.publishPost(postId);

        assertTrue(post.isPublished());
        assertNotNull(post.getPublishedAt());
        assertEquals(postId, post.getId());
        assertEquals(postId, postResponse.getId());
        assertTrue(postResponse.isPublished());
        assertTrue(Duration.between(post.getPublishedAt(), ZonedDateTime.now()).getSeconds() < 2);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertEquals(post, postCaptor.getValue());

        verify(postRepository, times(1)).findById(postId);
        verify(postValidator, times(1)).ensureCurrentActorIsAuthor(post);
        verify(postValidator, times(1)).ensureNotDeleted(post);
        verify(postValidator, times(1)).ensureNotPublished(post);
        verify(postRepository, times(1)).save(post);
        verify(postMapper, times(1)).toResponse(post);
    }

    @Test
    @DisplayName("Should throw DataValidationException when post is already published")
    void publishPost_shouldThrow_whenPostIsAlreadyPublished() {
        post.setDeleted(false);
        post.setPublished(true);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doThrow(DataValidationException.class).when(postValidator).ensureNotPublished(post);

        assertThrows(DataValidationException.class, () -> postService.publishPost(postId));

        verify(postRepository, times(1)).findById(postId);
        verify(postValidator, times(1)).ensureCurrentActorIsAuthor(post);
        verify(postValidator, times(1)).ensureNotDeleted(post);
        verify(postValidator, times(1)).ensureNotPublished(post);

        verifyNoMoreInteractions(postRepository);
        verify(postRepository, never()).save(any());
        verify(postMapper, never()).toResponse(any(Post.class));
    }

    @Test
    @DisplayName("Should throw DataValidationException when post is deleted")
    void publishPost_shouldThrow_whenPostDeleted() {
        post.setDeleted(true);
        post.setPublished(false);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doThrow(DataValidationException.class).when(postValidator).ensureNotDeleted(post);

        assertThrows(DataValidationException.class, () -> postService.publishPost(postId));

        verify(postRepository, times(1)).findById(postId);
        verify(postValidator, times(1)).ensureCurrentActorIsAuthor(post);
        verify(postValidator, times(1)).ensureNotDeleted(post);

        verifyNoMoreInteractions(postRepository);
        verify(postRepository, never()).save(any());
        verify(postMapper, never()).toResponse(any(Post.class));
    }

    @Test
    @DisplayName("Should updateComment post when it exists and input is valid")
    void updatePost_shouldUpdatePost_whenValid() {
        post.setDeleted(false);

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest();
        postUpdateRequest.setTitle("New title");
        postUpdateRequest.setContent("New content");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        postResponse = postService.updatePost(postId, postUpdateRequest);

        assertEquals(postId, postResponse.getId());
        assertEquals("New title", postResponse.getTitle());
        assertEquals("New content", postResponse.getContent());
        assertNotNull(postResponse.getUpdatedAt());

        verify(postRepository, times(1)).findById(postId);
        verify(postValidator, times(1)).ensureCurrentActorIsAuthor(post);
        verify(postValidator, times(1)).ensureNotDeleted(post);
        verify(postMapper, times(1)).update(post, postUpdateRequest);
        verify(postMapper, times(1)).toResponse(post);
    }

    @Test
    @DisplayName("Should mark the post as deleted when it exists and is valid")
    void deletePost_shouldMarkAsDeletedPost_whenValid() {
        post.setDeleted(false);
        post.setPublished(true);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        postResponse = postService.deletePost(postId);

        assertTrue(post.isDeleted());
        assertFalse(post.isPublished());

        assertEquals(postId, postResponse.getId());
        assertTrue(postResponse.isDeleted());
        assertFalse(postResponse.isPublished());

        verify(postRepository, times(1)).findById(postId);
        verify(postValidator, times(1)).ensureCurrentActorIsAuthor(post);
        verify(postValidator, times(1)).ensureNotDeleted(post);
        verify(postRepository, times(1)).save(post);
        verify(postMapper, times(1)).toResponse(post);
    }

    @Test
    @DisplayName("Should return post when it exists")
    void getPostById_shouldReturnPost_whenExists() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postResponse = postService.getPostById(postId, viewerUserId);

        assertEquals(post.getId(), postResponse.getId());
        assertEquals(post.getTitle(), postResponse.getTitle());
        assertEquals(post.getContent(), postResponse.getContent());
        assertEquals(post.getAuthorId(), postResponse.getAuthorId());
    }

    @Test
    @DisplayName("Should return list of posts when title matches")
    void getPostByTitle_shouldReturnPosts_whenTitleMatches() {
        String titlePart = "test";
        List<Post> posts = List.of(post);

        when(postRepository.findByTitle(titlePart)).thenReturn(posts);

        listPostResponse = postService.getPostByTitle(titlePart, viewerUserId);

        assertEquals(1, listPostResponse.size());

        postResponse = listPostResponse.get(0);
        assertEquals(post.getId(), postResponse.getId());
        assertEquals(post.getTitle(), postResponse.getTitle());
        assertEquals(post.getContent(), postResponse.getContent());
        assertEquals(post.getAuthorId(), postResponse.getAuthorId());

        verify(postRepository, times(1)).findByTitle(titlePart);
        verify(postMapper, times(1)).toResponseList(posts);
    }

    @Test
    @DisplayName("Should return drafts posts when author exists")
    void getDraftsByAuthorId_shouldReturnDrafts_whenAuthorExists() {
        List<Post> draftPosts = createPosts();

        when(postRepository.findDraftPostsByAuthor(userIdOrProjectId)).thenReturn(draftPosts);

        listPostResponse = postService.getDraftsByAuthorId(userIdOrProjectId, viewerUserId);

        assertPostResponseTitles(listPostResponse, "Post 1", "Post 2");

        verify(authorValidationService, times(1)).validateUserExists(userIdOrProjectId);
        verify(postRepository, times(1)).findDraftPostsByAuthor(userIdOrProjectId);
        verify(postMapper, times(1)).toResponseList(draftPosts);
    }

    @Test
    @DisplayName("Should return draft posts when project exists")
    void getDraftsByProjectId_shouldReturnDrafts_whenProjectExists() {
        List<Post> draftPosts = createPosts();

        when(postRepository.findDraftPostsByProject(userIdOrProjectId)).thenReturn(draftPosts);

        listPostResponse = postService.getDraftsByProjectId(userIdOrProjectId, viewerUserId);

        assertPostResponseTitles(listPostResponse, "Post 1", "Post 2");

        verify(authorValidationService, times(1)).validateProjectExists(userIdOrProjectId);
        verify(postRepository, times(1)).findDraftPostsByProject(userIdOrProjectId);
        verify(postMapper, times(1)).toResponseList(draftPosts);
    }

    @Test
    @DisplayName("Should return published posts when author exists")
    void getPostsByAuthorId_shouldReturnPublishedPosts_whenAuthorExists() {
        List<Post> postsPublished = createPosts();

        when(postRepository.findByAuthorIdWithLikes(userIdOrProjectId)).thenReturn(postsPublished);

        listPostResponse = postService.getPostsByAuthorId(userIdOrProjectId, viewerUserId);

        assertPostResponseTitles(listPostResponse, "Post 1", "Post 2");

        verify(authorValidationService, times(1)).validateUserExists(userIdOrProjectId);
        verify(postRepository, times(1)).findByAuthorIdWithLikes(userIdOrProjectId);
        verify(postMapper, times(1)).toResponseList(postsPublished);
    }

    @Test
    @DisplayName("Should return published posts when project exists")
    void getPostsByProjectId_shouldReturnPublishedPosts_whenProjectExists() {
        List<Post> postsPublished = createPosts();

        when(postRepository.findByProjectIdWithLikes(userIdOrProjectId)).thenReturn(postsPublished);

        listPostResponse = postService.getPostsByProjectId(userIdOrProjectId, viewerUserId);

        assertPostResponseTitles(listPostResponse, "Post 1", "Post 2");

        verify(authorValidationService, times(1)).validateProjectExists(userIdOrProjectId);
        verify(postRepository, times(1)).findByProjectIdWithLikes(userIdOrProjectId);
        verify(postMapper, times(1)).toResponseList(postsPublished);
    }

    @Test
    @DisplayName("Should return post when found by ID")
    void findPostOrThrow_shouldReturnPost_whenPostExists() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Post result = postService.findPostOrThrow(postId);

        assertEquals(post, result);
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when post not found")
    void findPostOrThrow_shouldThrowException_whenPostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class, () -> postService.findPostOrThrow(postId)
        );

        assertEquals("Post with ID " + postId + " not found", exception.getMessage());
        verify(postRepository, times(1)).findById(postId);
    }

    private List<Post> createPosts() {
        return List.of(
                Post.builder().id(1L).title("Post 1").content("Content 1").authorId(userIdOrProjectId).published(true).build(),
                Post.builder().id(2L).title("Post 2").content("Content 2").authorId(userIdOrProjectId).published(true).build()
        );
    }

    private void assertPostResponseTitles(List<PostResponse> responses, String... expectedTitles) {
        assertEquals(expectedTitles.length, responses.size());
        for (int i = 0; i < expectedTitles.length; i++) {
            assertEquals(expectedTitles[i], responses.get(i).getTitle());
        }
    }
}