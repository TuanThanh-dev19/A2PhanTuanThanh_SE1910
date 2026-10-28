// Author: Phan Tuan Thanh - CE190595
import { useContext } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthContext } from '../auth/AuthContext'
import { homePath } from '../auth/session'
import ManagementLayout from '../components/ManagementLayout'
import AccountsPage from '../pages/AccountsPage'
import LoginPage from '../pages/LoginPage'
import StaffPage from '../pages/StaffPage'
import ProtectedRoute from './ProtectedRoute'

export default function AppRoutes() {
  const { session } = useContext(AuthContext)
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute role={1} />}>
        <Route element={<ManagementLayout />}>
          <Route path="/accounts" element={<AccountsPage />} />
        </Route>
      </Route>
      <Route element={<ProtectedRoute role={2} />}>
        <Route element={<ManagementLayout />}>
          <Route path="/staff" element={<StaffPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to={homePath(session)} replace />} />
    </Routes>
  )
}
