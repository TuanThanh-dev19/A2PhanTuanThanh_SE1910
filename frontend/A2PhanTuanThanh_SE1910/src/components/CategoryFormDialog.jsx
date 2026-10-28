// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'

export default function CategoryFormDialog({ category, onClose, onSaved }) {
  const dialogRef = useRef(null)
  const savingRef = useRef(false)
  const isEditing = Boolean(category)
  const [categoryName, setCategoryName] = useState(category?.categoryName || '')
  const [categoryDescription, setCategoryDescription] = useState(category?.categoryDescription || '')
  const [parentCategoryId, setParentCategoryId] = useState(String(category?.parentCategoryId ?? ''))
  const [isActive, setIsActive] = useState(String(category?.isActive ?? 1))
  const [parents, setParents] = useState([])
  const [loadingParents, setLoadingParents] = useState(true)
  const [parentError, setParentError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  useEffect(() => {
    dialogRef.current.showModal()
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    async function loadParents() {
      try {
        // Parent choices must include categories outside the page's current search.
        const response = await client.get('/categories', { signal: controller.signal })
        if (!controller.signal.aborted) setParents(response.data)
      } catch {
        if (!controller.signal.aborted) setParentError('Không tải được danh mục cha. Vui lòng thử lại.')
      } finally {
        if (!controller.signal.aborted) setLoadingParents(false)
      }
    }
    loadParents()
    return () => controller.abort()
  }, [reloadCount])

  function handleCancel(event) {
    event.preventDefault()
    if (!savingRef.current) onClose()
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (savingRef.current || loadingParents || parentError) return
    const errors = {}
    if (!categoryName.trim()) errors.categoryName = 'Vui lòng nhập tên danh mục.'
    if (categoryName.trim().length > 100) errors.categoryName = 'Tên tối đa 100 ký tự.'
    if (categoryDescription.trim().length > 1000) errors.categoryDescription = 'Mô tả tối đa 1000 ký tự.'
    setFieldErrors(errors)
    setError('')
    if (Object.keys(errors).length > 0) return

    savingRef.current = true
    setSaving(true)
    try {
      const body = {
        categoryName: categoryName.trim(), categoryDescription: categoryDescription.trim(),
        parentCategoryId: parentCategoryId === '' ? null : Number(parentCategoryId), isActive: Number(isActive),
      }
      let response
      if (isEditing) {
        response = await client.put(`/categories/${category.categoryId}`, body)
      } else {
        response = await client.post('/categories', body)
      }
      onSaved(response.data)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Không lưu được danh mục. Vui lòng thử lại.')
      setFieldErrors(requestError.response?.data?.fieldErrors || {})
    } finally {
      savingRef.current = false
      setSaving(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog" aria-labelledby="category-form-title" onCancel={handleCancel}>
      <form onSubmit={handleSubmit} noValidate aria-busy={saving || loadingParents}>
        <h2 id="category-form-title">{isEditing ? 'Sửa danh mục' : 'Thêm danh mục'}</h2>
        {error && <p className="error-message" role="alert">{error}</p>}
        {loadingParents && <p role="status">Đang tải danh mục cha…</p>}
        {parentError && <div>
          <p className="error-message" role="alert">{parentError}</p>
          <button type="button" className="button-secondary" onClick={() => {
            setLoadingParents(true)
            setParentError('')
            setReloadCount((count) => count + 1)
          }}>Thử lại</button>
        </div>}
        <fieldset disabled={saving || loadingParents || Boolean(parentError)}>
          <label htmlFor="category-name">Tên</label>
          <input id="category-name" required maxLength={100} value={categoryName}
            onChange={(event) => setCategoryName(event.target.value)} aria-invalid={Boolean(fieldErrors.categoryName)}
            aria-describedby={fieldErrors.categoryName ? 'category-name-error' : undefined} />
          {fieldErrors.categoryName && <p id="category-name-error" className="field-error" role="alert">{fieldErrors.categoryName}</p>}
          <label htmlFor="category-description">Mô tả</label>
          <textarea id="category-description" rows={4} maxLength={1000} value={categoryDescription}
            onChange={(event) => setCategoryDescription(event.target.value)} aria-invalid={Boolean(fieldErrors.categoryDescription)}
            aria-describedby={fieldErrors.categoryDescription ? 'category-description-error' : undefined} />
          {fieldErrors.categoryDescription && <p id="category-description-error" className="field-error" role="alert">{fieldErrors.categoryDescription}</p>}
          <label htmlFor="category-parent">Danh mục cha</label>
          <select id="category-parent" value={parentCategoryId} onChange={(event) => setParentCategoryId(event.target.value)}
            aria-invalid={Boolean(fieldErrors.parentCategoryId)} aria-describedby={fieldErrors.parentCategoryId ? 'category-parent-error' : undefined}>
            <option value="">Không có danh mục cha</option>
            {parents.map((parent) => <option key={parent.categoryId} value={parent.categoryId}
              disabled={parent.categoryId === category?.categoryId}>
              {parent.categoryName} (ID: {parent.categoryId})
            </option>)}
          </select>
          {fieldErrors.parentCategoryId && <p id="category-parent-error" className="field-error" role="alert">{fieldErrors.parentCategoryId}</p>}
          <label htmlFor="category-status">Trạng thái</label>
          <select id="category-status" value={isActive} onChange={(event) => setIsActive(event.target.value)}
            aria-invalid={Boolean(fieldErrors.isActive)} aria-describedby={fieldErrors.isActive ? 'category-status-error' : undefined}>
            <option value="1">Active</option><option value="0">Inactive</option>
          </select>
          {fieldErrors.isActive && <p id="category-status-error" className="field-error" role="alert">{fieldErrors.isActive}</p>}
        </fieldset>
        <div className="dialog-actions">
          <button type="button" className="button-secondary" onClick={onClose} disabled={saving}>Hủy</button>
          <button type="submit" className="button-primary" disabled={saving || loadingParents || Boolean(parentError)}>
            {saving ? 'Đang lưu…' : 'Lưu danh mục'}
          </button>
        </div>
      </form>
    </dialog>
  )
}
