// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.repository;

import fpt.edu.a2phantuanthanh_se1910.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tag t WHERE t.tagId = :id")
    Optional<Tag> findForUpdate(@Param("id") Long id);

    @Query("""
            SELECT t FROM Tag t WHERE
            (LOWER(t.tagName) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
                OR LOWER(COALESCE(t.note, '')) LIKE CONCAT('%', :keyword, '%') ESCAPE '!')
            AND NOT EXISTS (SELECT link FROM NewsTag link WHERE link.tag = t
                AND (:articleId IS NULL OR link.newsArticle.newsArticleId <> :articleId))
            ORDER BY t.tagId
            """)
    List<Tag> searchAvailable(@Param("keyword") String keyword, @Param("articleId") Long articleId);
}
