// Author: Phan Tuan Thanh - CE190595
import { useContext, useEffect, useState } from 'react'
import client from '../api/client'
import AccountFormDialog from '../components/AccountFormDialog'
import AccountDeleteDialog from '../components/AccountDeleteDialog'
import { AuthContext } from '../auth/AuthContext'

export default function AccountsPage() {
  const { session, startSession, logout } = useContext(AuthContext)
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
  const [deleteAccount, setDeleteAccount] = useState(null)

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
            setError('You do not have permission to view accounts.')
          } else {
            setError('Unable to load accounts. Please try again.')
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
    setSuccess(wasEditing ? 'Account updated.' : 'Account created.')
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
      setActionError(requestError.response?.data?.message || 'Unable to load the account. Please try again.')
    } finally {
      setEditingId(null)
    }
  }

  function accountDeleted(accountId) {
    setDeleteAccount(null)
    if (accountId === session.account.accountId) {
      logout()
      return
    }
    setSuccess('Account deleted.')
    reloadAccounts()
  }

  return (
    <>
      <p className="eyebrow">System administration</p>
      <h1>Accounts</h1>
      <p className="muted page-description">Manage accounts and their roles.</p>
      <form className="account-toolbar" onSubmit={handleSearch}>
        <label htmlFor="account-search" className="sr-only">Search accounts by name or email</label>
        <input id="account-search" type="search" maxLength={200} placeholder="Search by name or email"
          value={keyword} onChange={changeKeyword} />
        <button type="submit" className="button-primary" disabled={loading}>Search</button>
        {(keyword || searchQuery) && <button type="button" className="button-secondary"
          onClick={clearSearch}>Clear search</button>}
        <button type="button" className="button-primary" onClick={() => {
          setSuccess('')
          setActionError('')
          setFormState({ account: null })
        }} disabled={editingId !== null}>Add account</button>
      </form>
      {success && <p className="notice" role="status">{success}</p>}
      {actionError && <p className="error-message" role="alert">{actionError}</p>}
      {formState && <AccountFormDialog account={formState.account}
        onClose={() => setFormState(null)} onSaved={accountSaved} />}
      {deleteAccount && <AccountDeleteDialog account={deleteAccount}
        onClose={() => setDeleteAccount(null)} onDeleted={accountDeleted} />}
      <section className="card" aria-labelledby="accounts-title" aria-busy={loading}>
        <div className="card-heading">
          <h2 id="accounts-title">Accounts</h2>
          {!loading && !error && <span className="count">Accounts: {accounts.length}</span>}
        </div>
        {loading && <p className="state-message" role="status">Loading accounts…</p>}
        {!loading && error && <div className="state-message">
          <p className="error-message" role="alert">{error}</p>
          <button type="button" className="button-secondary" onClick={reloadAccounts}>Try again</button>
        </div>}
        {!loading && !error && accounts.length === 0 &&
          <p className="state-message" role="status">
            {searchQuery ? 'No matching accounts found.' : 'No accounts yet.'}
          </p>}
        {!loading && !error && accounts.length > 0 && <div className="table-scroll">
          <table>
            <caption className="sr-only">Account ID, name, email and role</caption>
            <thead><tr><th scope="col">ID</th><th scope="col">Name</th><th scope="col">Email</th>
              <th scope="col">Role</th><th scope="col">Actions</th></tr></thead>
            <tbody>{accounts.map((account) => <tr key={account.accountId}>
              <td className="id-cell">{account.accountId}</td>
              <td className="name-cell">{account.accountName}</td>
              <td>{account.accountEmail}</td>
              <td><span className={`role-badge ${account.accountRole === 1 ? 'admin' : 'staff'}`}>
                {account.accountRole === 1 ? 'Admin' : 'Staff'}
              </span></td>
              <td><div className="row-actions">
                <button type="button" className="button-secondary" disabled={editingId !== null}
                  aria-label={`Edit ${account.accountEmail}`} onClick={() => openEditForm(account.accountId)}>
                  {editingId === account.accountId ? 'Loading…' : 'Edit'}
                </button>
                <button type="button" className="button-danger" disabled={editingId !== null}
                  aria-label={`Delete ${account.accountEmail}`} onClick={() => {
                    setSuccess('')
                    setActionError('')
                    setDeleteAccount(account)
                  }}>Delete</button>
              </div></td>
            </tr>)}</tbody>
          </table>
        </div>}
      </section>
    </>
  )
}
