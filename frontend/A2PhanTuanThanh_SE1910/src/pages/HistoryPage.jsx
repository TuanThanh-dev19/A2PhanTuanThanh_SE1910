// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import client from '../api/client'
import NewsDetailDialog from '../components/NewsDetailDialog'

export default function HistoryPage() {
  const [news, setNews] = useState([])
  const [keyword, setKeyword] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [detail, setDetail] = useState(null)
  const [openingId, setOpeningId] = useState(null)
  const [actionError, setActionError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    async function loadHistory() {
      try {
        const response = await client.get('/profile/news', {
          params: { q: searchQuery }, signal: controller.signal,
        })
        if (!controller.signal.aborted) setNews(response.data)
      } catch (requestError) {
        if (!controller.signal.aborted) {
          setError(requestError.response?.data?.message || 'Unable to load your news history. Please try again.')
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadHistory()
    return () => controller.abort()
  }, [searchQuery, reloadCount])

  function reloadHistory() {
    setError('')
    setActionError('')
    setLoading(true)
    setReloadCount((count) => count + 1)
  }

  function search(event) {
    event.preventDefault()
    setSearchQuery(keyword.trim())
    reloadHistory()
  }

  function clearSearch() {
    setKeyword('')
    setSearchQuery('')
    reloadHistory()
  }

  async function openDetail(id) {
    setOpeningId(id)
    setActionError('')
    try {
      const response = await client.get(`/news/${id}`)
      setDetail(response.data)
    } catch (requestError) {
      setActionError(requestError.response?.data?.message || 'Unable to load article details.')
    } finally {
      setOpeningId(null)
    }
  }

  return (
    <>
      <p className="eyebrow">My account</p>
      <h1>My news history</h1>
      <p className="muted page-description">Active and inactive articles you created, newest first.</p>
      <form className="account-toolbar" onSubmit={search}>
        <label className="sr-only" htmlFor="history-search">Search your articles by title, headline or content</label>
        <input id="history-search" type="search" maxLength={200} value={keyword}
          placeholder="Search by title, headline or content" onChange={(event) => {
            setKeyword(event.target.value)
            if (event.target.value.trim() === '' && searchQuery) clearSearch()
          }} />
        <button type="submit" className="button-primary" disabled={loading}>Search</button>
        {(keyword || searchQuery) && <button type="button" className="button-secondary" onClick={clearSearch}>Clear search</button>}
      </form>
      {actionError && <p className="error-message" role="alert">{actionError}</p>}
      {detail && <NewsDetailDialog news={detail} onClose={() => setDetail(null)} />}
      <section className="card" aria-busy={loading} aria-label="My articles">
        <div className="card-heading"><h2>My articles</h2>
          {!loading && !error && <span className="count">Articles: {news.length}</span>}</div>
        {loading && <p className="state-message" role="status">Loading your news history…</p>}
        {!loading && error && <div className="state-message"><p className="error-message" role="alert">{error}</p>
          <button type="button" className="button-secondary" onClick={reloadHistory}>Try again</button></div>}
        {!loading && !error && news.length === 0 && <p className="state-message" role="status">
          {searchQuery ? 'No matching articles found.' : 'You have not created any articles yet.'}</p>}
        {!loading && !error && news.length > 0 && <div className="table-scroll"><table>
          <caption className="sr-only">Your articles, including active and inactive articles</caption>
          <thead><tr><th scope="col">ID</th><th scope="col">Title</th><th scope="col">Category</th>
            <th scope="col">Status</th><th scope="col">Created (UTC)</th><th scope="col">Actions</th></tr></thead>
          <tbody>{news.map((article) => <tr key={article.newsArticleId}>
            <td>{article.newsArticleId}</td><td className="name-cell">{article.newsTitle}</td>
            <td>{article.categoryName}</td><td>{article.newsStatus === 1 ? 'Active' : 'Inactive'}</td>
            <td>{article.createdDate.replace('T', ' ')}</td>
            <td><div className="row-actions"><button type="button" className="button-secondary"
              disabled={openingId !== null} onClick={() => openDetail(article.newsArticleId)}>
              {openingId === article.newsArticleId ? 'Loading…' : 'Details'}</button></div></td>
          </tr>)}</tbody>
        </table></div>}
      </section>
    </>
  )
}
