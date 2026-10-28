// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import client from '../api/client'

export default function CategoriesPage() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    async function loadCategories() {
      try {
        const response = await client.get('/categories', {
          params: { q: searchQuery }, signal: controller.signal,
        })
        if (!controller.signal.aborted) setCategories(response.data)
      } catch (requestError) {
        if (!controller.signal.aborted) {
          if (requestError.response?.status === 403) {
            setError('Bạn không có quyền xem danh sách danh mục.')
          } else {
            setError('Không tải được danh sách danh mục. Vui lòng thử lại.')
          }
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadCategories()
    return () => controller.abort()
  }, [reloadCount, searchQuery])

  function reloadCategories() {
    setLoading(true)
    setError('')
    setReloadCount((count) => count + 1)
  }

  function handleSearch(event) {
    event.preventDefault()
    setSearchQuery(keyword.trim())
    reloadCategories()
  }

  function clearSearch() {
    setKeyword('')
    setSearchQuery('')
    reloadCategories()
  }

  function changeKeyword(event) {
    const value = event.target.value
    setKeyword(value)
    if (value.trim() === '' && searchQuery !== '') {
      setSearchQuery('')
      reloadCategories()
    }
  }

  return (
    <>
      <p className="eyebrow">Quản lý nội dung</p>
      <h1>Danh mục</h1>
      <p className="muted page-description">Danh sách danh mục đang hoạt động và ngừng hoạt động.</p>
      <form className="account-toolbar" onSubmit={handleSearch}>
        <label htmlFor="category-search" className="sr-only">Tìm danh mục theo tên hoặc mô tả</label>
        <input id="category-search" type="search" maxLength={200} placeholder="Tìm theo tên hoặc mô tả"
          value={keyword} onChange={changeKeyword} />
        <button type="submit" className="button-primary" disabled={loading}>Tìm kiếm</button>
        {(keyword || searchQuery) && <button type="button" className="button-secondary"
          onClick={clearSearch}>Xóa tìm kiếm</button>}
      </form>
      <section className="card" aria-labelledby="categories-title" aria-busy={loading}>
        <div className="card-heading">
          <h2 id="categories-title">Danh sách danh mục</h2>
          {!loading && !error && <span className="count">{categories.length} danh mục</span>}
        </div>
        {loading && <p className="state-message" role="status">Đang tải danh sách danh mục…</p>}
        {!loading && error && <div className="state-message">
          <p className="error-message" role="alert">{error}</p>
          <button type="button" className="button-secondary" onClick={reloadCategories}>Thử lại</button>
        </div>}
        {!loading && !error && categories.length === 0 &&
          <p className="state-message" role="status">
            {searchQuery ? 'Không tìm thấy danh mục phù hợp.' : 'Chưa có danh mục nào.'}
          </p>}
        {!loading && !error && categories.length > 0 && <div className="table-scroll">
          <table>
            <caption className="sr-only">ID, tên, mô tả, ID và tên danh mục cha, trạng thái</caption>
            <thead><tr><th scope="col">ID</th><th scope="col">Tên</th><th scope="col">Mô tả</th>
              <th scope="col">ID cha</th><th scope="col">Danh mục cha</th><th scope="col">Trạng thái</th></tr></thead>
            <tbody>{categories.map((category) => <tr key={category.categoryId}>
              <td className="id-cell">{category.categoryId}</td>
              <td className="name-cell">{category.categoryName}</td>
              <td>{category.categoryDescription || '—'}</td>
              <td className="id-cell">{category.parentCategoryId ?? '—'}</td>
              <td>{category.parentCategoryName ?? '—'}</td>
              <td>{category.isActive === 1 ? 'Active' : 'Inactive'}</td>
            </tr>)}</tbody>
          </table>
        </div>}
      </section>
    </>
  )
}
