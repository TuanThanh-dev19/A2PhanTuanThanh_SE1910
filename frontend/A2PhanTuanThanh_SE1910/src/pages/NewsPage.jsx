// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import client from '../api/client'
import NewsDetailDialog from '../components/NewsDetailDialog'
import NewsFormDialog from '../components/NewsFormDialog'
import NewsDeleteDialog from '../components/NewsDeleteDialog'

export default function NewsPage() {
  const [news, setNews] = useState([])
  const [keyword, setKeyword] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [detail, setDetail] = useState(null)
  const [openingId, setOpeningId] = useState(null)
  const [actionError, setActionError] = useState('')
  const [formState, setFormState] = useState(null)
  const [success, setSuccess] = useState('')
  const [deleteNews, setDeleteNews] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    async function loadNews() {
      try {
        const response = await client.get('/news', { params: { q: searchQuery }, signal: controller.signal })
        if (!controller.signal.aborted) setNews(response.data)
      } catch (requestError) {
        if (!controller.signal.aborted) {
          setError(requestError.response?.data?.message || 'Unable to load articles. Please try again.')
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadNews()
    return () => controller.abort()
  }, [searchQuery, reloadCount])

  function reloadNews() {
    setError('')
    setLoading(true)
    setReloadCount((count) => count + 1)
  }

  function search(event) {
    event.preventDefault()
    setSearchQuery(keyword.trim())
    reloadNews()
  }

  function clearSearch() {
    setKeyword('')
    setSearchQuery('')
    reloadNews()
  }

  async function openDetail(id, editing = false) {
    setOpeningId(id)
    setActionError('')
    try {
      const response = await client.get(`/news/${id}`)
      if (editing) {
        setFormState({ news: response.data })
      } else {
        setDetail(response.data)
      }
    } catch (requestError) {
      setActionError(requestError.response?.data?.message || 'Unable to load article details.')
    } finally {
      setOpeningId(null)
    }
  }

  return (
    <>
      <p className="eyebrow">Content management</p>
      <h1>Articles</h1>
      <p className="muted">Active and inactive articles created by all staff.</p>
      <form className="account-toolbar" onSubmit={search}>
        <label className="sr-only" htmlFor="news-search">Search articles by title, headline or content</label>
        <input id="news-search" type="search" maxLength={200} value={keyword}
          placeholder="Search by title, headline or content" onChange={(event) => {
            setKeyword(event.target.value)
            if (event.target.value.trim() === '' && searchQuery) clearSearch()
          }} />
        <button type="submit" className="button-primary" disabled={loading}>Search</button>
        {(keyword || searchQuery) && <button type="button" className="button-secondary" onClick={clearSearch}>Clear search</button>}
        <button type="button" className="button-primary" onClick={() => {
          setSuccess(''); setActionError(''); setFormState({ news: null })
        }} disabled={openingId !== null}>Add article</button>
      </form>
      {success && <p className="notice" role="status">{success}</p>}
      {formState && <NewsFormDialog news={formState.news} onClose={() => setFormState(null)} onSaved={() => {
        setSuccess(formState.news ? 'Article updated.' : 'Article created.')
        setFormState(null)
        reloadNews()
      }} />}
      {actionError && <p className="error-message" role="alert">{actionError}</p>}
      {detail && <NewsDetailDialog news={detail} onClose={() => setDetail(null)} />}
      {deleteNews && <NewsDeleteDialog news={deleteNews} onClose={() => setDeleteNews(null)} onDeleted={() => {
        setDeleteNews(null); setSuccess('Article deleted.'); reloadNews()
      }} />}
      <section className="card" aria-busy={loading} aria-label="Articles">
        <div className="card-heading"><h2>Articles</h2>
          {!loading && !error && <span className="count">Articles: {news.length}</span>}</div>
        {loading && <p className="state-message" role="status">Loading articles…</p>}
        {!loading && error && <div className="state-message"><p className="error-message" role="alert">{error}</p>
          <button type="button" className="button-secondary" onClick={reloadNews}>Try again</button></div>}
        {!loading && !error && news.length === 0 && <p className="state-message" role="status">
          {searchQuery ? 'No matching articles found.' : 'No articles yet.'}</p>}
        {!loading && !error && news.length > 0 && <div className="table-scroll"><table>
          <thead><tr><th scope="col">ID</th><th scope="col">Title</th><th scope="col">Category</th>
            <th scope="col">Status</th><th scope="col">Created by</th><th scope="col">Created (UTC)</th>
            <th scope="col">Actions</th></tr></thead>
          <tbody>{news.map((article) => <tr key={article.newsArticleId}>
            <td>{article.newsArticleId}</td><td className="name-cell">{article.newsTitle}</td>
            <td>{article.categoryName}</td><td>{article.newsStatus === 1 ? 'Active' : 'Inactive'}</td>
            <td>{article.createdByName}</td><td>{article.createdDate.replace('T', ' ')}</td>
            <td><div className="row-actions"><button type="button" className="button-secondary"
              disabled={openingId !== null} onClick={() => openDetail(article.newsArticleId)}>
              {openingId === article.newsArticleId ? 'Loading…' : 'Details'}</button>
              <button type="button" className="button-secondary" disabled={openingId !== null}
                onClick={() => openDetail(article.newsArticleId, true)}>Edit</button>
              <button type="button" className="button-danger" disabled={openingId !== null} onClick={() => {
                setSuccess(''); setActionError(''); setDeleteNews(article)
              }}>Delete</button></div></td>
          </tr>)}</tbody>
        </table></div>}
      </section>
    </>
  )
}
