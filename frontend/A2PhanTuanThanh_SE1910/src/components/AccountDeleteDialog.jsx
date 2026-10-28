// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'

export default function AccountDeleteDialog({ account, onClose, onDeleted }) {
  const dialogRef = useRef(null)
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    dialogRef.current.showModal()
  }, [])

  function handleCancel(event) {
    event.preventDefault()
    if (!deleting) onClose()
  }

  async function confirmDelete() {
    if (deleting) return
    setDeleting(true)
    setError('')
    try {
      await client.delete(`/accounts/${account.accountId}`)
      onDeleted(account.accountId)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Không xóa được tài khoản. Vui lòng thử lại.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog" aria-labelledby="delete-account-title" onCancel={handleCancel}>
      <h2 id="delete-account-title">Xóa tài khoản?</h2>
      <p>Bạn có chắc muốn xóa <strong>{account.accountName}</strong> ({account.accountEmail})?</p>
      <p className="muted">Tài khoản đã tạo bài viết sẽ không thể xóa.</p>
      {error && <p className="error-message" role="alert">{error}</p>}
      <div className="dialog-actions">
        <button type="button" className="button-secondary" onClick={onClose} disabled={deleting}>Hủy</button>
        <button type="button" className="button-danger" onClick={confirmDelete} disabled={deleting}>
          {deleting ? 'Đang xóa…' : 'Xác nhận xóa'}
        </button>
      </div>
    </dialog>
  )
}
