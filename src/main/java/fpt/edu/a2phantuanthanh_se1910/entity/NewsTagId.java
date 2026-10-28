// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class NewsTagId implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Positive
    @Column(name = "NewsArticleID", nullable = false)
    private Long newsArticleId;

    @NotNull
    @Positive
    @Column(name = "TagID", nullable = false)
    private Long tagId;

    protected NewsTagId() {
    }

    public NewsTagId(Long newsArticleId, Long tagId) {
        this.newsArticleId = newsArticleId;
        this.tagId = tagId;
    }

    public Long getNewsArticleId() {
        return newsArticleId;
    }

    public Long getTagId() {
        return tagId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NewsTagId otherId)) {
            return false;
        }
        return Objects.equals(newsArticleId, otherId.newsArticleId)
                && Objects.equals(tagId, otherId.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsArticleId, tagId);
    }
}
