// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'

export default function NewsDeleteDialog({ news, onClose, onDeleted }) {
  const dialogRef = useRef(null)
  const deletingRef = useRef(false)
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState('')
  useEffect(() => { dialogRef.current.showModal() }, [])

  function cancel(event) {
    event.preventDefault()
    if (!deletingRef.current) onClose()
  }

  async function confirmDelete() {
    if (deletingRef.current) return
    deletingRef.current = true
    setDeleting(true)
    setError('')
    try {
      await client.delete(`/news/${news.newsArticleId}`)
      onDeleted()
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Không xóa được bài viết. Vui lòng thử lại.')
    } finally {
      deletingRef.current = false
      setDeleting(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog" aria-labelledby="news-delete-title" onCancel={cancel}>
      <h2 id="news-delete-title">Xóa bài viết?</h2>
      <p>Bạn có chắc muốn xóa <strong>{news.newsTitle}</strong> (ID: {news.newsArticleId})?</p>
      <p className="muted">Tags được giữ lại để sử dụng cho bài viết khác.</p>
      {error && <p className="error-message" role="alert">{error}</p>}
      <div className="dialog-actions">
        <button type="button" className="button-secondary" disabled={deleting} onClick={onClose}>Hủy</button>
        <button type="button" className="button-danger" disabled={deleting} onClick={confirmDelete}>
          {deleting ? 'Đang xóa…' : 'Xác nhận xóa'}</button>
      </div>
    </dialog>
  )
}
