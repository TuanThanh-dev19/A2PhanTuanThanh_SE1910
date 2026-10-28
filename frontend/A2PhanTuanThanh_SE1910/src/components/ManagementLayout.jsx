// Author: Phan Tuan Thanh - CE190595
import { useContext } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { AuthContext } from '../auth/AuthContext'

export default function ManagementLayout() {
  const { session, logout } = useContext(AuthContext)
  const account = session.account
  const isAdmin = account.accountRole === 1

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand"><span className="brand-mark">FU</span>News Management</div>
        <div className="current-account">
          <div><strong>{account.accountName}</strong><span>{isAdmin ? 'Admin' : 'Staff'}</span></div>
          <button type="button" className="button-secondary" onClick={logout}>Đăng xuất</button>
        </div>
      </header>
      <div className="workspace">
        <nav aria-label="Điều hướng chính">
          <p className="eyebrow">Khu vực quản lý</p>
          {isAdmin && <NavLink to="/accounts">Tài khoản</NavLink>}
          {!isAdmin && <NavLink to="/staff">Trang Staff</NavLink>}
          {!isAdmin && <NavLink to="/categories">Danh mục</NavLink>}
        </nav>
        <main className="page-content"><Outlet /></main>
      </div>
    </div>
  )
}
