// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import client from '../api/client'
import CategoryFormDialog from '../components/CategoryFormDialog'
import CategoryDeleteDialog from '../components/CategoryDeleteDialog'

export default function CategoriesPage() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [formState, setFormState] = useState(null)
  const [success, setSuccess] = useState('')
  const [actionError, setActionError] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [deleteCategory, setDeleteCategory] = useState(null)

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
            setError('You do not have permission to view categories.')
          } else {
            setError('Unable to load categories. Please try again.')
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

  async function openEditForm(categoryId) {
    setEditingId(categoryId)
    setActionError('')
    setSuccess('')
    try {
      const response = await client.get(`/categories/${categoryId}`)
      setFormState({ category: response.data })
    } catch (requestError) {
      setActionError(requestError.response?.data?.message || 'Unable to load the category. Please try again.')
    } finally {
      setEditingId(null)
    }
  }

  function categorySaved() {
    setSuccess(formState.category ? 'Category updated.' : 'Category created.')
    setFormState(null)
    reloadCategories()
  }

  return (
    <>
      <p className="eyebrow">Content management</p>
      <h1>Categories</h1>
      <p className="muted page-description">Manage active and inactive categories.</p>
      <form className="account-toolbar" onSubmit={handleSearch}>
        <label htmlFor="category-search" className="sr-only">Search categories by name or description</label>
        <input id="category-search" type="search" maxLength={200} placeholder="Search by name or description"
          value={keyword} onChange={changeKeyword} />
        <button type="submit" className="button-primary" disabled={loading}>Search</button>
        {(keyword || searchQuery) && <button type="button" className="button-secondary"
          onClick={clearSearch}>Clear search</button>}
        <button type="button" className="button-primary" onClick={() => {
          setSuccess('')
          setActionError('')
          setFormState({ category: null })
        }} disabled={editingId !== null}>Add category</button>
      </form>
      {success && <p className="notice" role="status">{success}</p>}
      {actionError && <p className="error-message" role="alert">{actionError}</p>}
      {formState && <CategoryFormDialog category={formState.category}
        onClose={() => setFormState(null)} onSaved={categorySaved} />}
      {deleteCategory && <CategoryDeleteDialog category={deleteCategory}
        onClose={() => setDeleteCategory(null)} onDeleted={() => {
          setDeleteCategory(null)
          setSuccess('Category deleted.')
          reloadCategories()
        }} />}
      <section className="card" aria-labelledby="categories-title" aria-busy={loading}>
        <div className="card-heading">
          <h2 id="categories-title">Categories</h2>
          {!loading && !error && <span className="count">Categories: {categories.length}</span>}
        </div>
        {loading && <p className="state-message" role="status">Loading categories…</p>}
        {!loading && error && <div className="state-message">
          <p className="error-message" role="alert">{error}</p>
          <button type="button" className="button-secondary" onClick={reloadCategories}>Try again</button>
        </div>}
        {!loading && !error && categories.length === 0 &&
          <p className="state-message" role="status">
            {searchQuery ? 'No matching categories found.' : 'No categories yet.'}
          </p>}
        {!loading && !error && categories.length > 0 && <div className="table-scroll">
          <table>
            <caption className="sr-only">Category ID, name, description, parent ID, parent name and status</caption>
            <thead><tr><th scope="col">ID</th><th scope="col">Name</th><th scope="col">Description</th>
              <th scope="col">Parent ID</th><th scope="col">Parent category</th><th scope="col">Status</th>
              <th scope="col">Actions</th></tr></thead>
            <tbody>{categories.map((category) => <tr key={category.categoryId}>
              <td className="id-cell">{category.categoryId}</td>
              <td className="name-cell">{category.categoryName}</td>
              <td>{category.categoryDescription || '—'}</td>
              <td className="id-cell">{category.parentCategoryId ?? '—'}</td>
              <td>{category.parentCategoryName ?? '—'}</td>
              <td>{category.isActive === 1 ? 'Active' : 'Inactive'}</td>
              <td><div className="row-actions">
                <button type="button" className="button-secondary" disabled={editingId !== null}
                  aria-label={`Edit category ${category.categoryId}`} onClick={() => openEditForm(category.categoryId)}>
                  {editingId === category.categoryId ? 'Loading…' : 'Edit'}
                </button>
                <button type="button" className="button-danger" disabled={editingId !== null}
                  aria-label={`Delete category ${category.categoryId}`} onClick={() => {
                    setSuccess('')
                    setActionError('')
                    setDeleteCategory(category)
                  }}>Delete</button>
              </div></td>
            </tr>)}</tbody>
          </table>
        </div>}
      </section>
    </>
  )
}
