package faang.school.postservice.service;

import faang.school.postservice.dto.request.CommentCreateRequest;
import faang.school.postservice.dto.request.CommentUpdateRequest;
import faang.school.postservice.dto.response.CommentResponse;
import faang.school.postservice.event.CommentEvent;
import faang.school.postservice.mapper.CommentMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.validator.CommentValidation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {

    private final PostService postService;
    private final AuthorValidationService  authorValidationService;
    private final CommentValidation commentValidation;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentResponse createComment(long userId, CommentCreateRequest commentCreateRequest) {
        authorValidationService.validateUserExists(userId);
        Post post = postService.findPostOrThrow(commentCreateRequest.getPostId());

        Comment comment = commentMapper.toEntity(commentCreateRequest);
        comment.setAuthorId(userId);
        comment.setPost(post);
        comment = commentRepository.save(comment);

        log.info("Created comment with ID={} for postId={} by authorId={}",
                comment.getId(), comment.getPost().getId(), comment.getAuthorId());

        CommentEvent event = CommentEvent.builder()
                .postId(comment.getPost().getId())
                .commentId(comment.getId())
                .commentedAt(comment.getCreatedAt())
                .build();
        eventPublisher.publishEvent(event);

        return commentMapper.toResponse(comment);
    }

    @Transactional
    public CommentResponse updateComment(long commentId, CommentUpdateRequest commentUpdateRequest) {
        Comment comment = findCommentOrThrow(commentId);
        commentValidation.ensureCurrentActorIsAuthor(comment.getAuthorId());

        commentMapper.update(comment, commentUpdateRequest);
        comment.setUpdatedAt(ZonedDateTime.now());

        comment = commentRepository.save(comment);
        log.info("Updated comment with ID={} by authorId={}", comment.getId(), comment.getAuthorId());

        return commentMapper.toResponse(comment);
    }

    @Transactional
    public void deleteComment(long commentId) {
        Comment comment = findCommentOrThrow(commentId);
        commentValidation.ensureCurrentActorIsAuthor(comment.getAuthorId());

        commentRepository.deleteById(commentId);
        log.info("Deleted comment with ID={} by authorId={}", commentId, comment.getAuthorId());
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPostId(long postId, int page, int size) {
        log.info("Fetching comments for postId={}, page={}, size={}", postId, page, size);

        postService.findPostOrThrow(postId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> commentPage = commentRepository.findCommentsByPostId(postId, pageable);

        return commentPage.map(commentMapper::toResponse);
    }

    public Comment findCommentOrThrow(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID " + commentId + " not found"));
    }

}