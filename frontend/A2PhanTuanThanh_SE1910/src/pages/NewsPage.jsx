// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import client from '../api/client'
import NewsDetailDialog from '../components/NewsDetailDialog'

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

  useEffect(() => {
    const controller = new AbortController()
    async function loadNews() {
      try {
        const response = await client.get('/news', { params: { q: searchQuery }, signal: controller.signal })
        if (!controller.signal.aborted) setNews(response.data)
      } catch (requestError) {
        if (!controller.signal.aborted) {
          setError(requestError.response?.data?.message || 'Không tải được bài viết. Vui lòng thử lại.')
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

  async function openDetail(id) {
    setOpeningId(id)
    setActionError('')
    try {
      const response = await client.get(`/news/${id}`)
      setDetail(response.data)
    } catch (requestError) {
      setActionError(requestError.response?.data?.message || 'Không tải được chi tiết bài viết.')
    } finally {
      setOpeningId(null)
    }
  }

  return (
    <>
      <p className="eyebrow">Quản lý nội dung</p>
      <h1>Bài viết</h1>
      <p className="muted">Bài active và inactive của tất cả Staff.</p>
      <form className="account-toolbar" onSubmit={search}>
        <label className="sr-only" htmlFor="news-search">Tìm bài theo tiêu đề, headline hoặc nội dung</label>
        <input id="news-search" type="search" maxLength={200} value={keyword}
          placeholder="Tìm tiêu đề, headline hoặc nội dung" onChange={(event) => {
            setKeyword(event.target.value)
            if (event.target.value.trim() === '' && searchQuery) clearSearch()
          }} />
        <button type="submit" className="button-primary" disabled={loading}>Tìm kiếm</button>
        {(keyword || searchQuery) && <button type="button" className="button-secondary" onClick={clearSearch}>Xóa tìm kiếm</button>}
      </form>
      {actionError && <p className="error-message" role="alert">{actionError}</p>}
      {detail && <NewsDetailDialog news={detail} onClose={() => setDetail(null)} />}
      <section className="card" aria-busy={loading} aria-label="Danh sách bài viết">
        <div className="card-heading"><h2>Danh sách bài viết</h2>
          {!loading && !error && <span className="count">{news.length} bài viết</span>}</div>
        {loading && <p className="state-message" role="status">Đang tải bài viết…</p>}
        {!loading && error && <div className="state-message"><p className="error-message" role="alert">{error}</p>
          <button type="button" className="button-secondary" onClick={reloadNews}>Thử lại</button></div>}
        {!loading && !error && news.length === 0 && <p className="state-message" role="status">
          {searchQuery ? 'Không tìm thấy bài viết phù hợp.' : 'Chưa có bài viết nào.'}</p>}
        {!loading && !error && news.length > 0 && <div className="table-scroll"><table>
          <thead><tr><th scope="col">ID</th><th scope="col">Tiêu đề</th><th scope="col">Danh mục</th>
            <th scope="col">Trạng thái</th><th scope="col">Người tạo</th><th scope="col">Ngày tạo (UTC)</th>
            <th scope="col">Thao tác</th></tr></thead>
          <tbody>{news.map((article) => <tr key={article.newsArticleId}>
            <td>{article.newsArticleId}</td><td className="name-cell">{article.newsTitle}</td>
            <td>{article.categoryName}</td><td>{article.newsStatus === 1 ? 'Active' : 'Inactive'}</td>
            <td>{article.createdByName}</td><td>{article.createdDate.replace('T', ' ')}</td>
            <td><div className="row-actions"><button type="button" className="button-secondary"
              disabled={openingId !== null} onClick={() => openDetail(article.newsArticleId)}>
              {openingId === article.newsArticleId ? 'Đang tải…' : 'Chi tiết'}</button></div></td>
          </tr>)}</tbody>
        </table></div>}
      </section>
    </>
  )
}
