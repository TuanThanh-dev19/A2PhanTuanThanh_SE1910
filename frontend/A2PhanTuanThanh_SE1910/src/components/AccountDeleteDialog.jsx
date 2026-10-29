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
      setError(requestError.response?.data?.message || 'Unable to delete the account. Please try again.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog" aria-labelledby="delete-account-title" onCancel={handleCancel}>
      <h2 id="delete-account-title">Delete account?</h2>
      <p>Are you sure you want to delete <strong>{account.accountName}</strong> ({account.accountEmail})?</p>
      <p className="muted">Accounts that have created articles cannot be deleted.</p>
      {error && <p className="error-message" role="alert">{error}</p>}
      <div className="dialog-actions">
        <button type="button" className="button-secondary" onClick={onClose} disabled={deleting}>Cancel</button>
        <button type="button" className="button-danger" onClick={confirmDelete} disabled={deleting}>
          {deleting ? 'Deleting…' : 'Confirm delete'}
        </button>
      </div>
    </dialog>
  )
}
