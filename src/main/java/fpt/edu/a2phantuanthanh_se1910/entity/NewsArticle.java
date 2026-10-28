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

import java.time.LocalDateTime;

@Entity
@Table(name = "NewsArticle", schema = "dbo")
@Check(name = "CK_NewsArticle_Status", constraints = "NewsStatus IN (0, 1)")
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NewsArticleID", nullable = false, updatable = false)
    private Long newsArticleId;

    @NotBlank
    @Size(max = 200)
    @Nationalized
    @Column(name = "NewsTitle", length = 200, nullable = false)
    private String newsTitle;

    @NotBlank
    @Size(max = 300)
    @Nationalized
    @Column(name = "Headline", length = 300, nullable = false)
    private String headline;

    @NotNull
    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 20000)
    @Nationalized
    // Match SQL Server JDBC's nvarchar(max) length so update does not repeatedly ALTER the column.
    @Column(name = "NewsContent", nullable = false, length = Integer.MAX_VALUE)
    private String newsContent;

    @Size(max = 500)
    @Nationalized
    @Column(name = "NewsSource", length = 500, nullable = true)
    private String newsSource;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_NewsArticle_Category"))
    private Category category;

    @NotNull
    @Min(0)
    @Max(1)
    @Column(name = "NewsStatus", nullable = false)
    private Integer newsStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CreatedByID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_NewsArticle_Creator"))
    private SystemAccount createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "UpdatedByID", nullable = true,
            foreignKey = @ForeignKey(name = "FK_NewsArticle_Updater"))
    private SystemAccount updatedBy;

    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;

    protected NewsArticle() {
    }

    public NewsArticle(String newsTitle, String headline, LocalDateTime createdDate,
                       String newsContent, String newsSource, Category category,
                       Integer newsStatus, SystemAccount createdBy) {
        this.newsTitle = newsTitle;
        this.headline = headline;
        this.createdDate = createdDate;
        this.newsContent = newsContent;
        this.newsSource = newsSource;
        this.category = category;
        this.newsStatus = newsStatus;
        this.createdBy = createdBy;
    }

    public Long getNewsArticleId() {
        return newsArticleId;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getNewsContent() {
        return newsContent;
    }

    public void setNewsContent(String newsContent) {
        this.newsContent = newsContent;
    }

    public String getNewsSource() {
        return newsSource;
    }

    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getNewsStatus() {
        return newsStatus;
    }

    public void setNewsStatus(Integer newsStatus) {
        this.newsStatus = newsStatus;
    }

    public SystemAccount getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(SystemAccount createdBy) {
        this.createdBy = createdBy;
    }

    public SystemAccount getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(SystemAccount updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

}
