package faang.school.postservice.service;

import faang.school.postservice.dto.request.CommentCreateRequest;
import faang.school.postservice.dto.request.CommentUpdateRequest;
import faang.school.postservice.dto.response.CommentResponse;
import faang.school.postservice.event.CommentEvent;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.mapper.CommentMapperImpl;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Post;
import faang.school.postservice.publisher.redis.CommentEventPublisher;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.validator.CommentValidation;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private final long postId = 1L;
    private final long userId = 10L;
    private final long commentId = 100L;
    private final int page = 0;
    private final int size = 10;
    @Mock
    private AuthorValidationService authorValidationService;
    @Mock
    private PostService postService;
    @Mock
    private CommentValidation commentValidation;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentEventPublisher commentEventPublisher;
    @Spy
    private CommentMapperImpl commentMapper = new CommentMapperImpl();
    @InjectMocks
    private CommentService commentService;
    private Post post;
    private Comment savedComment;
    private CommentCreateRequest createRequest;
    private CommentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        post = Post.builder()
                .id(postId)
                .build();

        savedComment = Comment.builder()
                .id(commentId)
                .content("Test comment")
                .authorId(userId)
                .post(post)
                .createdAt(ZonedDateTime.now())
                .build();

        createRequest = CommentCreateRequest.builder()
                .content("Test comment")
                .postId(postId)
                .build();

        updateRequest = CommentUpdateRequest.builder()
                .content("Updated comment")
                .build();
    }

    @Test
    @DisplayName("Should create comment successfully when all data is valid")
    void createComment_shouldCreateComment_whenAllDataValid() {
        when(postService.findPostOrThrow(postId)).thenReturn(post);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse result = commentService.createComment(userId, createRequest);

        assertNotNull(result);
        assertEquals(savedComment.getId(), result.getId());
        assertEquals(savedComment.getContent(), result.getContent());
        assertEquals(savedComment.getAuthorId(), result.getAuthorId());
        assertEquals(savedComment.getPost().getId(), result.getPostId());

        verify(authorValidationService, times(1)).validateUserExists(userId);
        verify(postService, times(1)).findPostOrThrow(postId);
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(commentEventPublisher, times(1)).publish(any(CommentEvent.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when post not found during comment creation")
    void createComment_shouldThrow_whenPostNotFound() {
        doThrow(new EntityNotFoundException()).when(postService).findPostOrThrow(postId);

        assertThrows(EntityNotFoundException.class, () -> commentService.createComment(userId, createRequest));

        verify(authorValidationService, times(1)).validateUserExists(userId);
        verify(postService, times(1)).findPostOrThrow(postId);

        verifyNoMoreInteractions(commentRepository);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentEventPublisher, never()).publish(any(CommentEvent.class));
    }

    @Test
    @DisplayName("Should publish comment event after successful comment creation")
    void createComment_shouldPublishEvent_whenCommentCreated() {
        when(postService.findPostOrThrow(postId)).thenReturn(post);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        commentService.createComment(userId, createRequest);

        ArgumentCaptor<CommentEvent> eventCaptor = ArgumentCaptor.forClass(CommentEvent.class);
        verify(commentEventPublisher, times(1)).publish(eventCaptor.capture());
        CommentEvent event = eventCaptor.getValue();

        assertNotNull(event);
        assertEquals(savedComment.getId(), event.getCommentId());
        assertEquals(userId, event.getAuthorId());
        assertEquals(postId, event.getPostId());
        assertEquals(savedComment.getCreatedAt(), event.getCommentedAt());

        InOrder inOrder = Mockito.inOrder(commentRepository, commentEventPublisher);
        inOrder.verify(commentRepository, times(1)).save(any(Comment.class));
        inOrder.verify(commentEventPublisher, times(1)).publish(any(CommentEvent.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Should update comment successfully when all data is valid")
    void updateComment_shouldUpdateComment_whenAllDataValid() {
        savedComment.setUpdatedAt(ZonedDateTime.now().minusDays(1));

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(savedComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = commentService.updateComment(commentId, updateRequest);

        assertNotNull(response);
        assertEquals(commentId, response.getId());
        assertEquals("Updated comment", response.getContent());
        assertEquals(userId, response.getAuthorId());
        assertEquals(postId, response.getPostId());

        assertNotNull(savedComment.getUpdatedAt());
        assertTrue(savedComment.getUpdatedAt().isAfter(savedComment.getCreatedAt()));

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentValidation, times(1)).ensureCurrentActorIsAuthor(userId);
        verify(commentMapper, times(1)).update(savedComment, updateRequest);
        verify(commentRepository, times(1)).save(savedComment);
        verify(commentMapper, times(1)).toResponse(savedComment);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when comment not found during update")
    void updateComment_shouldThrow_whenCommentNotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertEntityNotFound(
                () -> commentService.updateComment(commentId, updateRequest)
        );
    }

    @Test
    @DisplayName("Should throw AuthorizationException when current user is not comment author during update")
    void updateComment_shouldThrow_whenUserNotAuthor() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(savedComment));

        doThrow(new DataValidationException("Only the author user can perform this action."))
                .when(commentValidation).ensureCurrentActorIsAuthor(savedComment.getAuthorId());

        assertEnsureCurrentActorIsAuthor(
                () -> commentService.updateComment(commentId, updateRequest)
        );
    }

    @Test
    @DisplayName("Should delete comment successfully when all conditions are met")
    void deleteComment_shouldDeleteComment_whenAllConditionsMet() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(savedComment));

        commentService.deleteComment(commentId);

        verify(commentValidation, times(1)).ensureCurrentActorIsAuthor(savedComment.getAuthorId());
        verify(commentRepository, times(1)).deleteById(savedComment.getId());

        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when comment not found during deletion")
    void deleteComment_shouldThrow_whenCommentNotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertEntityNotFound(
                () -> commentService.deleteComment(commentId)
        );
    }

    @Test
    @DisplayName("Should throw AuthorizationException when current user is not comment author during deletion")
    void deleteComment_shouldThrow_whenUserNotAuthor() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(savedComment));
        doThrow(new DataValidationException("Only the author user can perform this action."))
                .when(commentValidation).ensureCurrentActorIsAuthor(savedComment.getAuthorId());

        assertEnsureCurrentActorIsAuthor(
                () -> commentService.deleteComment(commentId)
        );
    }

    @Test
    @DisplayName("Should return comments page for existing post")
    void getCommentsByPostId_shouldReturnComments_whenPostExists() {
        Page<Comment> commentPage = new PageImpl<>(List.of(savedComment));
        CommentResponse commentResponse = CommentResponse.builder()
                .id(commentId)
                .content("Test comment")
                .authorId(userId)
                .postId(postId)
                .build();

        when(commentRepository.findCommentsByPostId(eq(postId), any(Pageable.class)))
                .thenReturn(commentPage);

        Page<CommentResponse> result = commentService.getCommentsByPostId(postId, page, size);
        CommentResponse actual = result.getContent().get(0);

        assertEquals(1, result.getTotalElements());
        assertEquals(commentResponse.getId(), actual.getId());
        assertEquals(commentResponse.getContent(), actual.getContent());
        assertEquals(commentResponse.getAuthorId(), actual.getAuthorId());
        assertEquals(commentResponse.getPostId(), actual.getPostId());

        verify(postService, times(1)).findPostOrThrow(postId);
        verify(commentRepository, times(1)).findCommentsByPostId(eq(postId), any(Pageable.class));
        verify(commentMapper, times(1)).toResponse(savedComment);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when post not found during comments fetching")
    void getCommentsByPostId_shouldThrow_whenPostNotFound() {
        doThrow(new EntityNotFoundException("Post with ID " + postId + " not found"))
                .when(postService).findPostOrThrow(postId);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                commentService.getCommentsByPostId(postId, page, size));

        assertEquals("Post with ID " + postId + " not found", ex.getMessage());

        verify(postService).findPostOrThrow(postId);
        verifyNoInteractions(commentRepository, commentMapper);
    }

    @Test
    @DisplayName("Should return empty page when no comments exist for post")
    void getCommentsByPostId_shouldReturnEmptyPage_whenNoComments() {
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList());

        when(commentRepository.findCommentsByPostId(eq(postId), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<CommentResponse> result = commentService.getCommentsByPostId(postId, page, size);

        assertTrue(result.isEmpty());

        verify(postService).findPostOrThrow(postId);
        verify(commentRepository).findCommentsByPostId(eq(postId), any(Pageable.class));
        verifyNoInteractions(commentMapper);
    }

    @Test
    @DisplayName("Should return comments sorted by createdAt in descending order")
    void getCommentsByPostId_shouldReturnSortedComments() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime earlier = now.minusHours(1);

        Comment olderComment = Comment.builder()
                .id(1L)
                .content("Older comment")
                .authorId(userId)
                .post(post)
                .createdAt(earlier)
                .build();

        Comment newerComment = Comment.builder()
                .id(2L)
                .content("Newer comment")
                .authorId(userId)
                .post(post)
                .createdAt(now)
                .build();

        Page<Comment> commentPage = new PageImpl<>(List.of(newerComment, olderComment));

        when(commentRepository.findCommentsByPostId(eq(postId), any(Pageable.class)))
                .thenReturn(commentPage);

        Page<CommentResponse> result = commentService.getCommentsByPostId(postId, page, size);

        assertEquals(2, result.getContent().size());
        assertEquals("Newer comment", result.getContent().get(0).getContent());
        assertEquals("Older comment", result.getContent().get(1).getContent());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findCommentsByPostId(eq(postId), pageableCaptor.capture());

        Pageable usedPageable = pageableCaptor.getValue();
        Sort.Order sortOrder = usedPageable.getSort().getOrderFor("createdAt");
        assertNotNull(sortOrder);
        assertEquals(Sort.Direction.DESC, sortOrder.getDirection());

        verify(commentMapper).toResponse(newerComment);
        verify(commentMapper).toResponse(olderComment);
    }

    @Test
    @DisplayName("Should find comment when it exists")
    void findCommentOrThrow_shouldReturnComment_whenExists() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(savedComment));

        Comment result = commentService.findCommentOrThrow(commentId);

        assertEquals(savedComment, result);
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when comment not found")
    void findCommentOrThrow_shouldThrow_whenCommentNotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertEntityNotFound(
                () -> commentService.findCommentOrThrow(commentId)
        );
    }

    private void assertEntityNotFound(Runnable operation) {
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, operation::run);
        assertEquals("Comment with ID " + commentId + " not found", ex.getMessage());

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentValidation, never()).ensureCurrentActorIsAuthor(userId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    private void assertEnsureCurrentActorIsAuthor(Runnable operation) {
        DataValidationException ex = assertThrows(DataValidationException.class, operation::run);
        assertEquals("Only the author user can perform this action.", ex.getMessage());

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentValidation, times(1)).ensureCurrentActorIsAuthor(savedComment.getAuthorId());
        verify(commentRepository, never()).save(any());
    }

}