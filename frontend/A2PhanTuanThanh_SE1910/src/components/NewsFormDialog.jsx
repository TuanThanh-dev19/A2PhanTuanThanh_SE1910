// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'
import NewsTagsEditor from './NewsTagsEditor'

export default function NewsFormDialog({ onClose, onSaved }) {
  const dialogRef = useRef(null)
  const savingRef = useRef(false)
  const [form, setForm] = useState({ newsTitle: '', headline: '', newsContent: '', newsSource: '', categoryId: '', newsStatus: '1' })
  const [tags, setTags] = useState([])
  const [categories, setCategories] = useState([])
  const [tagChoices, setTagChoices] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  useEffect(() => { dialogRef.current.showModal() }, [])
  useEffect(() => {
    const controller = new AbortController()
    async function loadChoices() {
      try {
        const [categoryResponse, tagResponse] = await Promise.all([
          client.get('/categories', { signal: controller.signal }),
          client.get('/tags', { signal: controller.signal }),
        ])
        if (!controller.signal.aborted) {
          setCategories(categoryResponse.data)
          setTagChoices(tagResponse.data)
        }
      } catch {
        if (!controller.signal.aborted) setLoadError('Không tải được danh mục hoặc tags. Vui lòng thử lại.')
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadChoices()
    return () => controller.abort()
  }, [reloadCount])

  function changeField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  function cancel(event) {
    event.preventDefault()
    if (!savingRef.current) onClose()
  }

  async function submit(event) {
    event.preventDefault()
    if (savingRef.current || loading || loadError) return
    const errors = {}
    if (!form.newsTitle.trim()) errors.newsTitle = 'Vui lòng nhập tiêu đề.'
    if (!form.headline.trim()) errors.headline = 'Vui lòng nhập headline.'
    if (!form.newsContent.trim()) errors.newsContent = 'Vui lòng nhập nội dung.'
    if (!form.categoryId) errors.categoryId = 'Vui lòng chọn danh mục.'
    if (form.newsTitle.trim().length > 200) errors.newsTitle = 'Tiêu đề tối đa 200 ký tự.'
    if (form.headline.trim().length > 300) errors.headline = 'Headline tối đa 300 ký tự.'
    if (form.newsContent.trim().length > 20000) errors.newsContent = 'Nội dung tối đa 20.000 ký tự.'
    if (form.newsSource.trim().length > 500) errors.newsSource = 'Nguồn tối đa 500 ký tự.'
    for (let index = 0; index < tags.length; index++) {
      if (!tags[index].tagName.trim() || tags[index].tagName.trim().length > 100) {
        errors[`tags[${index}].tagName`] = 'Tên tag bắt buộc, tối đa 100 ký tự.'
      }
      if (tags[index].note.trim().length > 500) errors[`tags[${index}].note`] = 'Ghi chú tối đa 500 ký tự.'
    }
    setFieldErrors(errors)
    setError('')
    if (Object.keys(errors).length > 0) return
    savingRef.current = true
    setSaving(true)
    try {
      const body = { ...form, categoryId: Number(form.categoryId), newsStatus: Number(form.newsStatus), tags }
      const response = await client.post('/news', body)
      onSaved(response.data)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Không lưu được bài viết. Vui lòng thử lại.')
      setFieldErrors(requestError.response?.data?.fieldErrors || {})
    } finally {
      savingRef.current = false
      setSaving(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog news-dialog" aria-labelledby="news-form-title" onCancel={cancel}>
      <form onSubmit={submit} noValidate aria-busy={loading || saving}>
        <h2 id="news-form-title">Thêm bài viết</h2>
        {loading && <p role="status">Đang tải danh mục và tags…</p>}
        {loadError && <div><p className="error-message" role="alert">{loadError}</p>
          <button type="button" className="button-secondary" onClick={() => {
            setLoading(true); setLoadError(''); setReloadCount((count) => count + 1)
          }}>Thử lại</button></div>}
        {error && <p className="error-message" role="alert">{error}</p>}
        <fieldset disabled={loading || saving || Boolean(loadError)}>
          <label htmlFor="news-title">Tiêu đề</label>
          <input id="news-title" name="newsTitle" maxLength={200} required value={form.newsTitle} onChange={changeField} />
          {fieldErrors.newsTitle && <p className="field-error" role="alert">{fieldErrors.newsTitle}</p>}
          <label htmlFor="news-headline">Headline</label>
          <input id="news-headline" name="headline" maxLength={300} required value={form.headline} onChange={changeField} />
          {fieldErrors.headline && <p className="field-error" role="alert">{fieldErrors.headline}</p>}
          <label htmlFor="news-content">Nội dung</label>
          <textarea id="news-content" name="newsContent" rows={8} maxLength={20000} required value={form.newsContent} onChange={changeField} />
          {fieldErrors.newsContent && <p className="field-error" role="alert">{fieldErrors.newsContent}</p>}
          <label htmlFor="news-source">Nguồn</label>
          <input id="news-source" name="newsSource" maxLength={500} value={form.newsSource} onChange={changeField} />
          {fieldErrors.newsSource && <p className="field-error" role="alert">{fieldErrors.newsSource}</p>}
          <label htmlFor="news-category">Danh mục</label>
          <select id="news-category" name="categoryId" required value={form.categoryId} onChange={changeField}>
            <option value="">Chọn danh mục</option>
            {categories.map((category) => <option key={category.categoryId} value={category.categoryId}>
              {category.categoryName} (ID: {category.categoryId}) — {category.isActive === 1 ? 'Active' : 'Inactive'}</option>)}
          </select>
          {fieldErrors.categoryId && <p className="field-error" role="alert">{fieldErrors.categoryId}</p>}
          <label htmlFor="news-status">Trạng thái</label>
          <select id="news-status" name="newsStatus" value={form.newsStatus} onChange={changeField}>
            <option value="1">Active</option><option value="0">Inactive</option>
          </select>
          {fieldErrors.newsStatus && <p className="field-error" role="alert">{fieldErrors.newsStatus}</p>}
          <NewsTagsEditor tags={tags} choices={tagChoices} onChange={setTags} errors={fieldErrors} />
        </fieldset>
        <div className="dialog-actions">
          <button type="button" className="button-secondary" disabled={saving} onClick={onClose}>Hủy</button>
          <button type="submit" className="button-primary" disabled={saving || loading || Boolean(loadError)}>
            {saving ? 'Đang lưu…' : 'Lưu bài viết'}</button>
        </div>
      </form>
    </dialog>
  )
}
