// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "Category", schema = "dbo")
@Check(name = "CK_Category_StatusAndParent",
        constraints = "IsActive IN (0, 1) AND (ParentCategoryID IS NULL OR ParentCategoryID <> CategoryID)")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID", nullable = false, updatable = false)
    private Long categoryId;

    @NotBlank
    @Size(max = 100)
    @Nationalized
    @Column(name = "CategoryName", length = 100, nullable = false)
    private String categoryName;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "CategoryDescription", length = 1000, nullable = true)
    private String categoryDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ParentCategoryID", nullable = true,
            foreignKey = @ForeignKey(name = "FK_Category_Parent"))
    private Category parentCategory;

    @NotNull
    @Min(0)
    @Max(1)
    @Column(name = "IsActive", nullable = false)
    private Integer isActive;

    protected Category() {
    }

    public Category(String categoryName, String categoryDescription, Category parentCategory,
                    Integer isActive) {
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.parentCategory = parentCategory;
        this.isActive = isActive;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

}
