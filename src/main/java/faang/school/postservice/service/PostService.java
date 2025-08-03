package faang.school.postservice.service;

import faang.school.postservice.dto.request.PostCreateRequest;
import faang.school.postservice.dto.request.PostUpdateRequest;
import faang.school.postservice.dto.response.PostResponse;
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

    private final PostValidator postValidator;
    private final AuthorValidationService authorValidationService;
    private final PostMapper postMapper;
    private final PostRepository postRepository;

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
    public PostResponse getPostById(long postId) {
        return postMapper.toResponse(findPostOrThrow(postId));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostByTitle(String titlePart) {
        List<Post> posts = postRepository.findByTitle(titlePart);
        return postMapper.toResponseList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getDraftsByAuthorId(long authorId) {
        authorValidationService.validateUserExists(authorId);

        List<Post> posts = postRepository.findDraftPostsByAuthor(authorId);
        return postMapper.toResponseList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getDraftsByProjectId(long projectId) {
        authorValidationService.validateProjectExists(projectId);

        List<Post> posts = postRepository.findDraftPostsByProject(projectId);
        return postMapper.toResponseList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByAuthorId(long authorId) {
        authorValidationService.validateUserExists(authorId);

        List<Post> posts = postRepository.findByAuthorIdWithLikes(authorId);
        return postMapper.toResponseList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByProjectId(long projectId) {
        authorValidationService.validateProjectExists(projectId);

        List<Post> posts = postRepository.findByProjectIdWithLikes(projectId);
        return postMapper.toResponseList(posts);
    }

    public Post findPostOrThrow(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post with ID " + postId + " not found"));
    }

}