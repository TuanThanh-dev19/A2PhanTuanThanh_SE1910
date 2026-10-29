// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import client from '../api/client'

export default function PublicNewsDetailPage() {
  const { id } = useParams()
  const [news, setNews] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notFound, setNotFound] = useState(false)
  const [reloadCount, setReloadCount] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    async function loadDetail() {
      setLoading(true)
      setError('')
      setNotFound(false)
      try {
        const response = await client.get(`/public/news/${id}`, { signal: controller.signal })
        if (!controller.signal.aborted) setNews(response.data)
      } catch (requestError) {
        if (!controller.signal.aborted) {
          if ([400, 404].includes(requestError.response?.status)) {
            setNotFound(true)
          } else {
            setError('Unable to load articles. Please try again.')
          }
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadDetail()
    return () => controller.abort()
  }, [id, reloadCount])

  return (
    <>
      <p><Link to="/public/news">← All news</Link></p>
      {loading && <p role="status">Loading articles…</p>}
      {!loading && notFound && <h1>Article not found</h1>}
      {!loading && error && <div><p className="error-message" role="alert">{error}</p>
        <button type="button" className="button-secondary" onClick={() => setReloadCount((count) => count + 1)}>Try again</button></div>}
      {!loading && !error && !notFound && news && <article className="card public-article">
        <p className="eyebrow">{news.categoryName}</p>
        <h1>{news.newsTitle}</h1>
        <p className="muted">{news.authorName} · {news.createdDate.replace('T', ' ')} UTC</p>
        <p><strong>{news.headline}</strong></p>
        <p className="news-content">{news.newsContent}</p>
        {news.newsSource && <p>Source: {news.newsSource}</p>}
        {news.tags.length > 0 && <p>Tags: {news.tags.map((tag) => tag.tagName).join(', ')}</p>}
      </article>}
    </>
  )
}
