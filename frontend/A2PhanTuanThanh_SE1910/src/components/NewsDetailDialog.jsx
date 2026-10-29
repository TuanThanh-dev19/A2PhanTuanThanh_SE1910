// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef } from 'react'

export default function NewsDetailDialog({ news, onClose }) {
  const dialogRef = useRef(null)
  useEffect(() => { dialogRef.current.showModal() }, [])

  return (
    <dialog ref={dialogRef} className="account-dialog news-dialog" aria-labelledby="news-detail-title"
      onCancel={(event) => { event.preventDefault(); onClose() }}>
      <h2 id="news-detail-title">{news.newsTitle}</h2>
      <p><strong>{news.headline}</strong></p>
      <p>{news.categoryName} · {news.newsStatus === 1 ? 'Active' : 'Inactive'}</p>
      <p className="news-content">{news.newsContent}</p>
      <p>Source: {news.newsSource || '—'}</p>
      <p>Created by: {news.createdByName} (ID: {news.createdById}) · {news.createdDate.replace('T', ' ')} UTC</p>
      <p>Updated by: {news.updatedByName || '—'} · {news.modifiedDate?.replace('T', ' ') || '—'} UTC</p>
      <h3>Tags</h3>
      {news.tags.length === 0 ? <p>No tags.</p> : <ul>{news.tags.map((tag) =>
        <li key={tag.tagId}>{tag.tagName}{tag.note && ` — ${tag.note}`}</li>)}</ul>}
      <div className="dialog-actions"><button type="button" className="button-secondary" onClick={onClose}>Close</button></div>
    </dialog>
  )
}
