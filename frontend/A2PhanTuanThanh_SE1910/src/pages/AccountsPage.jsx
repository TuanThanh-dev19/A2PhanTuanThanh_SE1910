// Author: Phan Tuan Thanh - CE190595
import { useContext, useEffect, useState } from 'react'
import client from '../api/client'
import AccountFormDialog from '../components/AccountFormDialog'
import { AuthContext } from '../auth/AuthContext'

export default function AccountsPage() {
  const { session, startSession } = useContext(AuthContext)
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [formState, setFormState] = useState(null)
  const [success, setSuccess] = useState('')
  const [actionError, setActionError] = useState('')
  const [editingId, setEditingId] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    async function loadAccounts() {
      try {
        const response = await client.get('/accounts', {
          params: { q: searchQuery }, signal: controller.signal,
        })
        if (!controller.signal.aborted) setAccounts(response.data)
      } catch (requestError) {
        if (!controller.signal.aborted) {
          if (requestError.response?.status === 403) {
            setError('Bạn không có quyền xem danh sách tài khoản.')
          } else {
            setError('Không tải được danh sách tài khoản. Vui lòng thử lại.')
          }
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadAccounts()
    return () => controller.abort()
  }, [reloadCount, searchQuery])

  function reloadAccounts() {
    setLoading(true)
    setError('')
    setReloadCount((count) => count + 1)
  }

  function handleSearch(event) {
    event.preventDefault()
    setSearchQuery(keyword.trim())
    reloadAccounts()
  }

  function clearSearch() {
    setKeyword('')
    setSearchQuery('')
    reloadAccounts()
  }

  function changeKeyword(event) {
    const value = event.target.value
    setKeyword(value)
    if (value.trim() === '' && searchQuery !== '') {
      setSearchQuery('')
      reloadAccounts()
    }
  }

  function accountSaved(savedAccount) {
    const wasEditing = Boolean(formState.account)
    setFormState(null)
    setSuccess(wasEditing ? 'Đã cập nhật tài khoản.' : 'Đã thêm tài khoản.')
    if (savedAccount.accountId === session.account.accountId) {
      startSession({ ...session, account: savedAccount })
    }
    reloadAccounts()
  }

  async function openEditForm(accountId) {
    setEditingId(accountId)
    setActionError('')
    setSuccess('')
    try {
      const response = await client.get(`/accounts/${accountId}`)
      setFormState({ account: response.data })
    } catch (requestError) {
      setActionError(requestError.response?.data?.message || 'Không tải được tài khoản. Vui lòng thử lại.')
    } finally {
      setEditingId(null)
    }
  }

  return (
    <>
      <p className="eyebrow">Quản trị hệ thống</p>
      <h1>Tài khoản</h1>
      <p className="muted page-description">Danh sách tài khoản và vai trò trong hệ thống.</p>
      <form className="account-toolbar" onSubmit={handleSearch}>
        <label htmlFor="account-search" className="sr-only">Tìm tài khoản theo tên hoặc email</label>
        <input id="account-search" type="search" maxLength={200} placeholder="Tìm theo tên hoặc email"
          value={keyword} onChange={changeKeyword} />
        <button type="submit" className="button-primary" disabled={loading}>Tìm kiếm</button>
        {(keyword || searchQuery) && <button type="button" className="button-secondary"
          onClick={clearSearch}>Xóa tìm kiếm</button>}
        <button type="button" className="button-primary" onClick={() => {
          setSuccess('')
          setActionError('')
          setFormState({ account: null })
        }} disabled={editingId !== null}>Thêm tài khoản</button>
      </form>
      {success && <p className="notice" role="status">{success}</p>}
      {actionError && <p className="error-message" role="alert">{actionError}</p>}
      {formState && <AccountFormDialog account={formState.account}
        onClose={() => setFormState(null)} onSaved={accountSaved} />}
      <section className="card" aria-labelledby="accounts-title" aria-busy={loading}>
        <div className="card-heading">
          <h2 id="accounts-title">Danh sách tài khoản</h2>
          {!loading && !error && <span className="count">{accounts.length} tài khoản</span>}
        </div>
        {loading && <p className="state-message" role="status">Đang tải danh sách tài khoản…</p>}
        {!loading && error && <div className="state-message">
          <p className="error-message" role="alert">{error}</p>
          <button type="button" className="button-secondary" onClick={reloadAccounts}>Thử lại</button>
        </div>}
        {!loading && !error && accounts.length === 0 &&
          <p className="state-message" role="status">
            {searchQuery ? 'Không tìm thấy tài khoản phù hợp.' : 'Chưa có tài khoản nào.'}
          </p>}
        {!loading && !error && accounts.length > 0 && <div className="table-scroll">
          <table>
            <caption className="sr-only">ID, tên, email và vai trò của các tài khoản</caption>
            <thead><tr><th scope="col">ID</th><th scope="col">Tên</th><th scope="col">Email</th>
              <th scope="col">Vai trò</th><th scope="col">Thao tác</th></tr></thead>
            <tbody>{accounts.map((account) => <tr key={account.accountId}>
              <td className="id-cell">{account.accountId}</td>
              <td className="name-cell">{account.accountName}</td>
              <td>{account.accountEmail}</td>
              <td><span className={`role-badge ${account.accountRole === 1 ? 'admin' : 'staff'}`}>
                {account.accountRole === 1 ? 'Admin' : 'Staff'}
              </span></td>
              <td><button type="button" className="button-secondary" disabled={editingId !== null}
                aria-label={`Sửa ${account.accountEmail}`} onClick={() => openEditForm(account.accountId)}>
                {editingId === account.accountId ? 'Đang tải…' : 'Sửa'}
              </button></td>
            </tr>)}</tbody>
          </table>
        </div>}
      </section>
    </>
  )
}
