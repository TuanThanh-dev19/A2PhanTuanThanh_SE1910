// Author: Phan Tuan Thanh - CE190595
import { useContext } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthContext } from '../auth/AuthContext'
import { homePath } from '../auth/session'
import ManagementLayout from '../components/ManagementLayout'
import AccountsPage from '../pages/AccountsPage'
import CategoriesPage from '../pages/CategoriesPage'
import LoginPage from '../pages/LoginPage'
import StaffPage from '../pages/StaffPage'
import NewsPage from '../pages/NewsPage'
import ProfilePage from '../pages/ProfilePage'
import PublicLayout from '../components/PublicLayout'
import PublicNewsPage from '../pages/PublicNewsPage'
import PublicNewsDetailPage from '../pages/PublicNewsDetailPage'
import ProtectedRoute from './ProtectedRoute'

export default function AppRoutes() {
  const { session } = useContext(AuthContext)
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/public/news" replace />} />
      <Route element={<PublicLayout />}>
        <Route path="/public/news" element={<PublicNewsPage />} />
        <Route path="/public/news/:id" element={<PublicNewsDetailPage />} />
      </Route>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute role={1} />}>
        <Route element={<ManagementLayout />}>
          <Route path="/accounts" element={<AccountsPage />} />
        </Route>
      </Route>
      <Route element={<ProtectedRoute role={2} />}>
        <Route element={<ManagementLayout />}>
          <Route path="/staff" element={<StaffPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/news" element={<NewsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to={homePath(session)} replace />} />
    </Routes>
  )
}
