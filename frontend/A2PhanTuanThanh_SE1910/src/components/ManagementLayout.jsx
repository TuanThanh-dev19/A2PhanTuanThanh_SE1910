// Author: Phan Tuan Thanh - CE190595
import { useContext } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
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
          <Link to="/public/news">Public news</Link>
          <div><strong>{account.accountName}</strong><span>{isAdmin ? 'Admin' : 'Staff'}</span></div>
          <button type="button" className="button-secondary" onClick={logout}>Sign out</button>
        </div>
      </header>
      <div className="workspace">
        <nav aria-label="Main navigation">
          <p className="eyebrow">Management</p>
          {isAdmin && <NavLink to="/accounts">Accounts</NavLink>}
          {!isAdmin && <NavLink to="/staff">Staff home</NavLink>}
          {!isAdmin && <NavLink to="/categories">Categories</NavLink>}
          {!isAdmin && <NavLink to="/news">Articles</NavLink>}
        </nav>
        <main className="page-content"><Outlet /></main>
      </div>
    </div>
  )
}
