package faang.school.postservice.service;

import faang.school.postservice.dto.request.PostCreateRequest;
import faang.school.postservice.dto.request.PostUpdateRequest;
import faang.school.postservice.dto.response.PostResponse;
import faang.school.postservice.helper.PostAnalyticsEventHelper;
import faang.school.postservice.mapper.PostMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.validator.PostValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

    private final AuthorValidationService authorValidationService;
    private final PostValidator postValidator;
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final PostAnalyticsEventHelper postAnalyticsEventHelper;

    @Transactional
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        postValidator.validateAuthor(postCreateRequest);

        Post post = postMapper.toEntity(postCreateRequest);
        post.setVerified(true);
        post = postRepository.save(post);
        return postMapper.toResponse(post);
    }

    @Transactional
    public PostResponse publishPost(long postId) {
        Post post = findPostOrThrow(postId);

        postValidator.ensureCurrentActorIsAuthor(post);
        postValidator.ensureNotDeleted(post);
        postValidator.ensureNotPublished(post);

        post.setPublished(true);
        post.setPublishedAt(ZonedDateTime.now());

        postRepository.save(post);
        return postMapper.toResponse(post);
    }

    @Transactional
    public PostResponse updatePost(long postId, PostUpdateRequest postUpdateRequest) {
        Post post = findPostOrThrow(postId);

        postValidator.ensureCurrentActorIsAuthor(post);
        postValidator.ensureNotDeleted(post);

        postMapper.update(post, postUpdateRequest);
        post.setUpdatedAt(ZonedDateTime.now());

        post = postRepository.save(post);
        return postMapper.toResponse(post);
    }

    @Transactional
    public PostResponse deletePost(long postId) {
        Post post = findPostOrThrow(postId);

        postValidator.ensureCurrentActorIsAuthor(post);
        postValidator.ensureNotDeleted(post);

        post.setPublished(false);
        post.setDeleted(true);

        post = postRepository.save(post);
        return postMapper.toResponse(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(long postId, long viewerUserId) {
        Post post = findPostOrThrow(postId);

        postAnalyticsEventHelper.publishPostViewEvent(post, viewerUserId);

        return postMapper.toResponse(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostByTitle(String titlePart, long viewerUserId) {
        List<Post> posts = postRepository.findByTitle(titlePart);

        postAnalyticsEventHelper.publishPostViewEvents(posts, viewerUserId);

        return postMapper.toResponseList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getDraftsByAuthorId(long authorId, long viewerUserId) {
        authorValidationService.validateUserExists(authorId);

        List<Post> posts = postRepository.findDraftPostsByAuthor(authorId);
        postAnalyticsEventHelper.publishPostViewEvents(posts, viewerUserId);

        return postMapper.toResponseList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getDraftsByProjectId(long projectId, long viewerUserId) {
        authorValidationService.validateProjectExists(projectId);

        List<Post> posts = postRepository.findDraftPostsByProject(projectId);
        postAnalyticsEventHelper.publishPostViewEvents(posts, viewerUserId);

        return postMapper.toResponseList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByAuthorId(long authorId, long viewerUserId) {
        authorValidationService.validateUserExists(authorId);

        List<Post> posts = postRepository.findByAuthorIdWithLikes(authorId);
        postAnalyticsEventHelper.publishPostViewEvents(posts, viewerUserId);

        return postMapper.toResponseList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByProjectId(long projectId, long viewerUserId) {
        authorValidationService.validateProjectExists(projectId);

        List<Post> posts = postRepository.findByProjectIdWithLikes(projectId);
        postAnalyticsEventHelper.publishPostViewEvents(posts, viewerUserId);

        return postMapper.toResponseList(posts);
    }

    public Post findPostOrThrow(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post with ID " + postId + " not found"));
    }

}