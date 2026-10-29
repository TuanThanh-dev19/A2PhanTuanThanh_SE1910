// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.repository;

import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    @Query(value = """
            SELECT n.* FROM dbo.NewsArticle n
            WHERE LOWER(n.NewsTitle) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
                OR LOWER(n.Headline) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
                OR LOWER(n.NewsContent) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
            ORDER BY n.CreatedDate DESC, n.NewsArticleID DESC
            """, nativeQuery = true)
    List<NewsArticle> searchNews(@Param("keyword") String keyword);

    // Native SQL avoids Hibernate treating nvarchar(max) content as NCLOB in lower().
    @Query(value = """
            SELECT n.* FROM dbo.NewsArticle n
            WHERE n.NewsStatus = 1 AND
                (LOWER(n.NewsTitle) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
                OR LOWER(n.Headline) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
                OR LOWER(n.NewsContent) LIKE CONCAT('%', :keyword, '%') ESCAPE '!')
            ORDER BY n.CreatedDate DESC, n.NewsArticleID DESC
            """, nativeQuery = true)
    List<NewsArticle> searchActiveNews(@Param("keyword") String keyword);

    boolean existsByNewsTitleAndCreatedBy_AccountId(String newsTitle, Long accountId);

    boolean existsByCreatedBy_AccountId(Long accountId);

    boolean existsByCategory_CategoryId(Long categoryId);

    boolean existsByUpdatedBy_AccountId(Long accountId);

    List<NewsArticle> findByCreatedBy_AccountIdOrderByCreatedDateDescNewsArticleIdDesc(Long accountId);

    List<NewsArticle> findByUpdatedBy_AccountId(Long accountId);

    List<NewsArticle> findByNewsStatusOrderByCreatedDateDescNewsArticleIdDesc(Integer newsStatus);

    Optional<NewsArticle> findByNewsArticleIdAndNewsStatus(Long newsArticleId, Integer newsStatus);
}
