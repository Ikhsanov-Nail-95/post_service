package faang.school.postservice.mapper;

import faang.school.postservice.dto.request.PostCreateRequest;
import faang.school.postservice.dto.request.PostUpdateRequest;
import faang.school.postservice.dto.response.PostResponse;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.Post;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    Post toEntity(PostCreateRequest postCreateRequest);

    void update(@MappingTarget Post post, PostUpdateRequest postUpdateRequest);

    @Mapping(source = "likes", target = "likeIds", qualifiedByName = "mapLikesToLikeIds")
    @Mapping(source = "comments", target = "commentsIds", qualifiedByName = "mapCommentsToCommentIds")
    @Mapping(expression = "java(post.getLikes() != null ? post.getLikes().size() : 0)", target = "likeCount")
    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> posts);

    @Named("mapLikesToLikeIds")
    default List<Long> mapLikesToLikeIds(List<Like> likes) {
        if (likes == null || likes.isEmpty()) {
            return List.of();
        }
        return likes.stream()
                .map(Like::getId)
                .toList();
    }

    @Named("mapCommentsToCommentIds")
    default List<Long> mapCommentsToCommentIds(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }
        return comments.stream()
                .map(Comment::getId)
                .toList();
    }

}