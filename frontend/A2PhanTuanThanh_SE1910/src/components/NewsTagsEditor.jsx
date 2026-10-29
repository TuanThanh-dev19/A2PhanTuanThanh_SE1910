// Author: Phan Tuan Thanh - CE190595
import { useState } from 'react'

export default function NewsTagsEditor({ tags, choices, onChange, errors }) {
  const [selectedId, setSelectedId] = useState('')

  function changeTag(index, field, value) {
    const updated = tags.map((tag, position) => {
      if (position === index) return { ...tag, [field]: value }
      return tag
    })
    onChange(updated)
  }

  function attachTag() {
    const selected = choices.find((tag) => String(tag.tagId) === selectedId)
    if (!selected || tags.some((tag) => tag.tagId === selected.tagId)) return
    onChange([...tags, { ...selected, note: selected.note || '' }])
    setSelectedId('')
  }

  return (
    <section aria-label="Article tags">
      <h3>Tags</h3>
      <label htmlFor="existing-tag">Existing tag</label>
      <select id="existing-tag" value={selectedId} onChange={(event) => setSelectedId(event.target.value)}>
        <option value="">Select a tag to attach</option>
        {choices.filter((choice) => !tags.some((tag) => tag.tagId === choice.tagId)).map((tag) =>
          <option key={tag.tagId} value={tag.tagId}>{tag.tagName} (ID: {tag.tagId})</option>)}
      </select>
      <div className="row-actions tag-actions">
        <button type="button" className="button-secondary" onClick={attachTag} disabled={!selectedId}>Attach tag</button>
        <button type="button" className="button-secondary" onClick={() =>
          onChange([...tags, { tagId: null, tagName: '', note: '' }])}>Add new tag</button>
      </div>
      {tags.length === 0 && <p className="muted">This article has no tags.</p>}
      {tags.map((tag, index) => <div className="tag-entry" key={tag.tagId || `new-${index}`}>
        <label htmlFor={`tag-name-${index}`}>Tag name {index + 1}</label>
        <input id={`tag-name-${index}`} value={tag.tagName} maxLength={100} required
          onChange={(event) => changeTag(index, 'tagName', event.target.value)}
          aria-invalid={Boolean(errors[`tags[${index}].tagName`])} />
        {errors[`tags[${index}].tagName`] && <p className="field-error" role="alert">{errors[`tags[${index}].tagName`]}</p>}
        <label htmlFor={`tag-note-${index}`}>Tag note {index + 1}</label>
        <textarea id={`tag-note-${index}`} value={tag.note || ''} maxLength={500} rows={2}
          onChange={(event) => changeTag(index, 'note', event.target.value)} />
        {errors[`tags[${index}].note`] && <p className="field-error" role="alert">{errors[`tags[${index}].note`]}</p>}
        <button type="button" className="button-secondary" onClick={() =>
          onChange(tags.filter((_, position) => position !== index))}>Remove tag {index + 1}</button>
      </div>)}
    </section>
  )
}
