// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client from '../api/client'

export default function PublicNewsPage() {
  const [news, setNews] = useState([])
  const [keyword, setKeyword] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    async function loadNews() {
      try {
        const response = await client.get('/public/news', { params: { q: searchQuery }, signal: controller.signal })
        if (!controller.signal.aborted) setNews(response.data)
      } catch (requestError) {
        if (!controller.signal.aborted) setError(requestError.response?.data?.message || 'Unable to load news. Please try again.')
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadNews()
    return () => controller.abort()
  }, [searchQuery, reloadCount])

  function reloadNews() {
    setError(''); setLoading(true); setReloadCount((count) => count + 1)
  }

  function clearSearch() {
    setKeyword(''); setSearchQuery(''); reloadNews()
  }

  return (
    <>
      <p className="eyebrow">FUNewsManagementSystem</p>
      <h1>News</h1>
      <p className="muted">The latest news and announcements from the university.</p>
      <form className="account-toolbar" onSubmit={(event) => {
        event.preventDefault(); setSearchQuery(keyword.trim()); reloadNews()
      }}>
        <label className="sr-only" htmlFor="public-search">Search news by title, headline or content</label>
        <input id="public-search" type="search" maxLength={200} value={keyword}
          placeholder="Search by title, headline or content" onChange={(event) => {
            setKeyword(event.target.value)
            if (!event.target.value.trim() && searchQuery) clearSearch()
          }} />
        <button type="submit" className="button-primary" disabled={loading}>Search</button>
        {(keyword || searchQuery) && <button type="button" className="button-secondary" onClick={clearSearch}>Clear search</button>}
      </form>
      {loading && <p className="state-message" role="status">Loading news…</p>}
      {!loading && error && <div className="state-message"><p className="error-message" role="alert">{error}</p>
        <button type="button" className="button-secondary" onClick={reloadNews}>Try again</button></div>}
      {!loading && !error && news.length === 0 && <p className="state-message" role="status">
        {searchQuery ? 'No matching news found.' : 'No news yet.'}</p>}
      {!loading && !error && <div className="news-grid">{news.map((article) =>
        <article className="card public-article" key={article.newsArticleId}>
          <p className="eyebrow">{article.categoryName}</p>
          <h2><Link to={`/public/news/${article.newsArticleId}`}>{article.newsTitle}</Link></h2>
          <p>{article.headline}</p>
          <p className="muted">{article.createdDate.replace('T', ' ')} UTC</p>
          <Link className="button-link button-secondary" to={`/public/news/${article.newsArticleId}`}>
            Read article <span aria-hidden="true">→</span>
          </Link>
        </article>)}</div>}
    </>
  )
}
