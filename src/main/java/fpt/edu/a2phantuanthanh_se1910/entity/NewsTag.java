// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
@Table(name = "NewsTag", schema = "dbo", uniqueConstraints = {
        // DEC-01 is provisional: a tag can belong to at most one article.
        @UniqueConstraint(name = "UQ_NewsTag_TagID", columnNames = "TagID")
})
public class NewsTag {

    @EmbeddedId
    @NotNull
    @Valid
    private NewsTagId id;

    @MapsId("newsArticleId")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NewsArticleID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_NewsTag_NewsArticle"))
    private NewsArticle newsArticle;

    @MapsId("tagId")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TagID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_NewsTag_Tag"))
    private Tag tag;

    protected NewsTag() {
    }

    public NewsTag(NewsArticle newsArticle, Tag tag) {
        this.newsArticle = Objects.requireNonNull(newsArticle, "News article is required");
        this.tag = Objects.requireNonNull(tag, "Tag is required");
        this.id = new NewsTagId(
                Objects.requireNonNull(newsArticle.getNewsArticleId(), "Save the article before linking"),
                Objects.requireNonNull(tag.getTagId(), "Save the tag before linking"));
    }

    public NewsTagId getId() {
        return id;
    }

    public NewsArticle getNewsArticle() {
        return newsArticle;
    }

    public Tag getTag() {
        return tag;
    }
}
