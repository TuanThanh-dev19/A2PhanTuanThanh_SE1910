// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.repository;

import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    boolean existsByCreatedBy_AccountId(Long accountId);

    boolean existsByCategory_CategoryId(Long categoryId);

    boolean existsByUpdatedBy_AccountId(Long accountId);

    List<NewsArticle> findByCreatedBy_AccountIdOrderByCreatedDateDescNewsArticleIdDesc(Long accountId);

    List<NewsArticle> findByUpdatedBy_AccountId(Long accountId);

    List<NewsArticle> findByNewsStatusOrderByCreatedDateDescNewsArticleIdDesc(Integer newsStatus);

    Optional<NewsArticle> findByNewsArticleIdAndNewsStatus(Long newsArticleId, Integer newsStatus);
}
