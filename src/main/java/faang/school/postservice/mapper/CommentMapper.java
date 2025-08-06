package faang.school.postservice.mapper;

import faang.school.postservice.dto.request.CommentCreateRequest;
import faang.school.postservice.dto.request.CommentUpdateRequest;
import faang.school.postservice.dto.response.CommentResponse;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "post", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.ZonedDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.ZonedDateTime.now())")
    Comment toEntity(CommentCreateRequest commentCreateRequest);

    void update(@MappingTarget Comment comment, CommentUpdateRequest commentUpdateRequest);

    @Mapping(source = "likes", target = "likeIds", qualifiedByName = "mapLikesToLikeIds")
    @Mapping(source = "post.id", target = "postId")
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);

    @Named("mapLikesToLikeIds")
    default List<Long> mapLikesToLikeIds(List<Like> likes) {
        if (likes == null || likes.isEmpty()) {
            return List.of();
        }
        return likes.stream()
                .map(Like::getId)
                .toList();
    }

}