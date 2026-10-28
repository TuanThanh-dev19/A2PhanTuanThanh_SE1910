// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import client from '../api/client'

export default function AccountsPage() {
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    async function loadAccounts() {
      try {
        const response = await client.get('/accounts', { signal: controller.signal })
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
  }, [reloadCount])

  function reloadAccounts() {
    setLoading(true)
    setError('')
    setReloadCount(reloadCount + 1)
  }

  return (
    <>
      <p className="eyebrow">Quản trị hệ thống</p>
      <h1>Tài khoản</h1>
      <p className="muted page-description">Danh sách tài khoản và vai trò trong hệ thống.</p>
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
          <p className="state-message" role="status">Chưa có tài khoản nào.</p>}
        {!loading && !error && accounts.length > 0 && <div className="table-scroll">
          <table>
            <caption className="sr-only">ID, tên, email và vai trò của các tài khoản</caption>
            <thead><tr><th scope="col">ID</th><th scope="col">Tên</th><th scope="col">Email</th><th scope="col">Vai trò</th></tr></thead>
            <tbody>{accounts.map((account) => <tr key={account.accountId}>
              <td className="id-cell">{account.accountId}</td>
              <td className="name-cell">{account.accountName}</td>
              <td>{account.accountEmail}</td>
              <td><span className={`role-badge ${account.accountRole === 1 ? 'admin' : 'staff'}`}>
                {account.accountRole === 1 ? 'Admin' : 'Staff'}
              </span></td>
            </tr>)}</tbody>
          </table>
        </div>}
      </section>
    </>
  )
}
