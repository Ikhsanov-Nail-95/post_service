package faang.school.postservice.repository;

import faang.school.postservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthorId(long authorId);

    List<Post> findByProjectId(long projectId);

    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :titlePart, '%'))")
    List<Post> findByTitle(String titlePart);

    void deleteAllByAuthorIdIn(List<Long> authorIds);

    @Query("""
        SELECT p FROM Post p
        LEFT JOIN FETCH p.likes
        WHERE p.projectId = :projectId AND p.published = true AND p.deleted = false
        ORDER BY p.publishedAt DESC
    """)
    List<Post> findByProjectIdWithLikes(long projectId);

    @Query("""
        SELECT p FROM Post p
        LEFT JOIN FETCH p.likes
        WHERE p.authorId = :authorId AND p.published = true AND p.deleted = false
        ORDER BY p.publishedAt DESC
    """)
    List<Post> findByAuthorIdWithLikes(long authorId);

    @Query("SELECT p FROM Post p WHERE p.published = false AND p.deleted = false AND p.scheduledAt <= CURRENT_TIMESTAMP")
    List<Post> findReadyToPublish();

    List<Post> findAllByVerified(boolean isVerified);

    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId AND p.published = false AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Post> findDraftPostsByAuthor(Long authorId);

    @Query("SELECT p FROM Post p WHERE p.projectId = :projectId AND p.published = false AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Post> findDraftPostsByProject(long projectId);
}