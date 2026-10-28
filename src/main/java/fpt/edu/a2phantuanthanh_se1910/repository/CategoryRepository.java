// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.repository;

import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentCategory_CategoryId(Long parentCategoryId);
}
