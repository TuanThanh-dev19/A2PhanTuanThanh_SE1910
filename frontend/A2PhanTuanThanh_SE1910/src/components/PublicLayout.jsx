// Author: Phan Tuan Thanh - CE190595
import { useContext } from 'react'
import { Link, Outlet } from 'react-router-dom'
import { AuthContext } from '../auth/AuthContext'
import { homePath } from '../auth/session'

export default function PublicLayout() {
  const { session } = useContext(AuthContext)
  return (
    <div className="app-shell">
      <header className="app-header">
        <Link className="brand" to="/public/news"><span className="brand-mark">FU</span>News Management</Link>
        <Link className="button-link button-primary" to={homePath(session)}>
          {session ? 'Management' : 'Sign in'}
        </Link>
      </header>
      <main className="public-content"><Outlet /></main>
    </div>
  )
}
