// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'

export default function CategoryDeleteDialog({ category, onClose, onDeleted }) {
  const dialogRef = useRef(null)
  const deletingRef = useRef(false)
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    dialogRef.current.showModal()
  }, [])

  function handleCancel(event) {
    event.preventDefault()
    if (!deletingRef.current) onClose()
  }

  async function confirmDelete() {
    if (deletingRef.current) return
    deletingRef.current = true
    setDeleting(true)
    setError('')
    try {
      await client.delete(`/categories/${category.categoryId}`)
      onDeleted()
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Unable to delete the category. Please try again.')
    } finally {
      deletingRef.current = false
      setDeleting(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog" aria-labelledby="delete-category-title" onCancel={handleCancel}>
      <h2 id="delete-category-title">Delete category?</h2>
      <p>Are you sure you want to delete <strong>{category.categoryName}</strong> (ID: {category.categoryId})?</p>
      <p className="muted">Categories used by articles cannot be deleted. Child categories will be kept without a parent.</p>
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
