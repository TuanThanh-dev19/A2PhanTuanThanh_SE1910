// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.repository;

import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            SELECT c FROM Category c LEFT JOIN FETCH c.parentCategory
            WHERE LOWER(c.categoryName) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
                OR LOWER(c.categoryDescription) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
            ORDER BY c.categoryId
            """)
    List<Category> search(@Param("keyword") String keyword);

    Optional<Category> findFirstByCategoryName(String categoryName);

    List<Category> findByParentCategory_CategoryId(Long parentCategoryId);
}
