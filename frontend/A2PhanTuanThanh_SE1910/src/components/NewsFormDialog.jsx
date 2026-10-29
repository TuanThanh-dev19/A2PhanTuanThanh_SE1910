// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'
import NewsTagsEditor from './NewsTagsEditor'

export default function NewsFormDialog({ news, onClose, onSaved }) {
  const dialogRef = useRef(null)
  const savingRef = useRef(false)
  const [form, setForm] = useState({ newsTitle: news?.newsTitle || '', headline: news?.headline || '',
    newsContent: news?.newsContent || '', newsSource: news?.newsSource || '',
    categoryId: String(news?.categoryId || ''), newsStatus: String(news?.newsStatus ?? 1) })
  const [tags, setTags] = useState(() => (news?.tags || []).map((tag) => ({ ...tag, note: tag.note || '' })))
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
          client.get('/tags', { params: { newsArticleId: news?.newsArticleId }, signal: controller.signal }),
        ])
        if (!controller.signal.aborted) {
          setCategories(categoryResponse.data)
          setTagChoices(tagResponse.data)
        }
      } catch {
        if (!controller.signal.aborted) setLoadError('Unable to load categories or tags. Please try again.')
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadChoices()
    return () => controller.abort()
  }, [reloadCount, news?.newsArticleId])

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
    if (!form.newsTitle.trim()) errors.newsTitle = 'Please enter a title.'
    if (!form.headline.trim()) errors.headline = 'Please enter a headline.'
    if (!form.newsContent.trim()) errors.newsContent = 'Please enter the content.'
    if (!form.categoryId) errors.categoryId = 'Please select a category.'
    if (form.newsTitle.trim().length > 200) errors.newsTitle = 'Title must be at most 200 characters.'
    if (form.headline.trim().length > 300) errors.headline = 'Headline must be at most 300 characters.'
    if (form.newsContent.trim().length > 20000) errors.newsContent = 'Content must be at most 20,000 characters.'
    if (form.newsSource.trim().length > 500) errors.newsSource = 'Source must be at most 500 characters.'
    for (let index = 0; index < tags.length; index++) {
      if (!tags[index].tagName.trim() || tags[index].tagName.trim().length > 100) {
        errors[`tags[${index}].tagName`] = 'Tag name is required and must be at most 100 characters.'
      }
      if (tags[index].note.trim().length > 500) errors[`tags[${index}].note`] = 'Note must be at most 500 characters.'
    }
    setFieldErrors(errors)
    setError('')
    if (Object.keys(errors).length > 0) return
    savingRef.current = true
    setSaving(true)
    try {
      const body = { ...form, categoryId: Number(form.categoryId), newsStatus: Number(form.newsStatus), tags }
      let response
      if (news) {
        response = await client.put(`/news/${news.newsArticleId}`, body)
      } else {
        response = await client.post('/news', body)
      }
      onSaved(response.data)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Unable to save the article. Please try again.')
      setFieldErrors(requestError.response?.data?.fieldErrors || {})
    } finally {
      savingRef.current = false
      setSaving(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog news-dialog" aria-labelledby="news-form-title" onCancel={cancel}>
      <form onSubmit={submit} noValidate aria-busy={loading || saving}>
        <h2 id="news-form-title">{news ? 'Edit article' : 'Add article'}</h2>
        {loading && <p role="status">Loading categories and tags…</p>}
        {loadError && <div><p className="error-message" role="alert">{loadError}</p>
          <button type="button" className="button-secondary" onClick={() => {
            setLoading(true); setLoadError(''); setReloadCount((count) => count + 1)
          }}>Try again</button></div>}
        {error && <p className="error-message" role="alert">{error}</p>}
        <fieldset disabled={loading || saving || Boolean(loadError)}>
          <label htmlFor="news-title">Title</label>
          <input id="news-title" name="newsTitle" maxLength={200} required value={form.newsTitle} onChange={changeField} />
          {fieldErrors.newsTitle && <p className="field-error" role="alert">{fieldErrors.newsTitle}</p>}
          <label htmlFor="news-headline">Headline</label>
          <input id="news-headline" name="headline" maxLength={300} required value={form.headline} onChange={changeField} />
          {fieldErrors.headline && <p className="field-error" role="alert">{fieldErrors.headline}</p>}
          <label htmlFor="news-content">Content</label>
          <textarea id="news-content" name="newsContent" rows={8} maxLength={20000} required value={form.newsContent} onChange={changeField} />
          {fieldErrors.newsContent && <p className="field-error" role="alert">{fieldErrors.newsContent}</p>}
          <label htmlFor="news-source">Source</label>
          <input id="news-source" name="newsSource" maxLength={500} value={form.newsSource} onChange={changeField} />
          {fieldErrors.newsSource && <p className="field-error" role="alert">{fieldErrors.newsSource}</p>}
          <label htmlFor="news-category">Category</label>
          <select id="news-category" name="categoryId" required value={form.categoryId} onChange={changeField}>
            <option value="">Select a category</option>
            {categories.map((category) => <option key={category.categoryId} value={category.categoryId}>
              {category.categoryName} (ID: {category.categoryId}) — {category.isActive === 1 ? 'Active' : 'Inactive'}</option>)}
          </select>
          {fieldErrors.categoryId && <p className="field-error" role="alert">{fieldErrors.categoryId}</p>}
          <label htmlFor="news-status">Status</label>
          <select id="news-status" name="newsStatus" value={form.newsStatus} onChange={changeField}>
            <option value="1">Active</option><option value="0">Inactive</option>
          </select>
          {fieldErrors.newsStatus && <p className="field-error" role="alert">{fieldErrors.newsStatus}</p>}
          <NewsTagsEditor tags={tags} choices={tagChoices} onChange={setTags} errors={fieldErrors} />
        </fieldset>
        <div className="dialog-actions">
          <button type="button" className="button-secondary" disabled={saving} onClick={onClose}>Cancel</button>
          <button type="submit" className="button-primary" disabled={saving || loading || Boolean(loadError)}>
            {saving ? 'Saving…' : 'Save article'}</button>
        </div>
      </form>
    </dialog>
  )
}
