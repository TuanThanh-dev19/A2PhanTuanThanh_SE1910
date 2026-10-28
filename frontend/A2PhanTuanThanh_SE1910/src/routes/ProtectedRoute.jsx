// Author: Phan Tuan Thanh - CE190595
import { useContext } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { AuthContext } from '../auth/AuthContext'
import { homePath } from '../auth/session'

export default function ProtectedRoute({ role }) {
  const { session } = useContext(AuthContext)
  if (!session) return <Navigate to="/login" replace />
  if (session.account.accountRole !== role) {
    return <Navigate to={homePath(session)} replace />
  }
  return <Outlet />
}
