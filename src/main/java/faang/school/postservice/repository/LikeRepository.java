package faang.school.postservice.repository;

import faang.school.postservice.model.Like;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("SELECT l.userId FROM Like l WHERE l.post.id = :postId")
    List<Long> findUserIdsByPostId(@Param("postId") long postId);

    @Query("SELECT l.userId FROM Like l WHERE l.comment.id = :commentId")
    List<Long> findUserIdsByCommentId(@Param("commentId") long commentId);

    void deleteByUserIdAndPostId(long userId, long postId);

    void deleteByUserIdAndCommentId(long userId, long commentId);

    boolean existsByUserIdAndPostId(long userId, long postId);

    boolean existsByUserIdAndCommentId(long userId, long commentId);

    Optional<Like> findByUserIdAndPostId(long userId, long postId);

    Optional<Like> findByUserIdAndCommentId(long userId, long commentId);

    List<Like> findByPostId(long postId);

    List<Like> findByCommentId(long commentId);
}