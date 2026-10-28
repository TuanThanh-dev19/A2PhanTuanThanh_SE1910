// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.repository;

import fpt.edu.a2phantuanthanh_se1910.entity.NewsTag;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsTagRepository extends JpaRepository<NewsTag, NewsTagId> {

    List<NewsTag> findByNewsArticle_NewsArticleId(Long newsArticleId);

    boolean existsByTag_TagId(Long tagId);

    Optional<NewsTag> findByTag_TagId(Long tagId);
}
