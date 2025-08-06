package faang.school.postservice.mapper;

import faang.school.postservice.dto.response.LikeResponse;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.enums.LikeTargetType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LikeMapper {
    @Mapping(target = "entityId", expression = "java(getEntityId(like))")
    @Mapping(target = "targetType", expression = "java(getTargetType(like))")
    LikeResponse toResponse(Like like);

    default Long getEntityId(Like like) {
        return like.getPost() != null ? like.getPost().getId()
                                      : like.getComment().getId();
    }

    default LikeTargetType getTargetType(Like like) {
        return like.getPost() != null ? LikeTargetType.POST
                                      : LikeTargetType.COMMENT;
    }
}