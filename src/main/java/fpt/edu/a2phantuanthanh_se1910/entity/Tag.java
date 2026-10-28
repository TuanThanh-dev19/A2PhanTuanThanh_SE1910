// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "Tag", schema = "dbo")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TagID", nullable = false, updatable = false)
    private Long tagId;

    @NotBlank
    @Size(max = 100)
    @Nationalized
    @Column(name = "TagName", length = 100, nullable = false)
    private String tagName;

    @Size(max = 500)
    @Nationalized
    @Column(name = "Note", length = 500, nullable = true)
    private String note;

    protected Tag() {
    }

    public Tag(String tagName, String note) {
        this.tagName = tagName;
        this.note = note;
    }

    public Long getTagId() {
        return tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
