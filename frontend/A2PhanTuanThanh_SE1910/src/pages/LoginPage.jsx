// Author: Phan Tuan Thanh - CE190595
import { useContext, useState } from 'react'
import { Navigate } from 'react-router-dom'
import client from '../api/client'
import { AuthContext } from '../auth/AuthContext'
import { homePath } from '../auth/session'

export default function LoginPage() {
  const { session, notice, startSession } = useContext(AuthContext)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  if (session) return <Navigate to={homePath(session)} replace />

  async function handleSubmit(event) {
    event.preventDefault()
    if (submitting) return
    setError('')
    setFieldErrors({})
    if (new TextEncoder().encode(password).length > 72) {
      setFieldErrors({ password: 'Mật khẩu tối đa 72 byte UTF-8.' })
      return
    }
    setSubmitting(true)
    try {
      const response = await client.post('/auth/login', { email: email.trim(), password })
      startSession(response.data)
    } catch (requestError) {
      if (requestError.response?.status === 401) {
        setError('Email hoặc mật khẩu không đúng.')
      } else if (requestError.response?.status === 400) {
        setFieldErrors(requestError.response.data.fieldErrors || {})
        setError(requestError.response.data.message || 'Vui lòng kiểm tra thông tin đăng nhập.')
      } else {
        setError('Không thể đăng nhập. Vui lòng kiểm tra kết nối và thử lại.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-shell">
      <section className="login-intro">
        <div className="brand"><span className="brand-mark">FU</span>News Management</div>
        <div><p className="eyebrow">FUNewsManagementSystem</p>
          <h1>Kết nối thông tin.<br />Quản lý dễ dàng.</h1>
          <p>Đăng nhập để truy cập khu vực làm việc dành cho tài khoản của bạn.</p>
        </div>
        <small>SBA301 · Assignment 02</small>
      </section>
      <section className="login-panel" aria-labelledby="login-title">
        <p className="eyebrow">Chào mừng trở lại</p>
        <h2 id="login-title">Đăng nhập</h2>
        <p className="muted">Sử dụng email và mật khẩu của bạn.</p>
        {notice && <p className="notice" role="status">{notice}</p>}
        {error && <p className="error-message" role="alert">{error}</p>}
        <form onSubmit={handleSubmit} aria-busy={submitting}>
          <label htmlFor="email">Email</label>
          <input id="email" type="email" autoComplete="username" required maxLength={254}
            value={email} onChange={(event) => setEmail(event.target.value)} disabled={submitting}
            aria-invalid={Boolean(fieldErrors.email)} aria-describedby={fieldErrors.email ? 'email-error' : undefined} />
          {fieldErrors.email && <p id="email-error" className="field-error">{fieldErrors.email}</p>}
          <label htmlFor="password">Mật khẩu</label>
          <input id="password" type="password" autoComplete="current-password" required maxLength={72}
            value={password} onChange={(event) => setPassword(event.target.value)} disabled={submitting}
            aria-invalid={Boolean(fieldErrors.password)} aria-describedby={fieldErrors.password ? 'password-error' : undefined} />
          {fieldErrors.password && <p id="password-error" className="field-error">{fieldErrors.password}</p>}
          <button type="submit" className="button-primary" disabled={submitting}>
            {submitting ? 'Đang đăng nhập…' : 'Đăng nhập'}
          </button>
        </form>
      </section>
    </main>
  )
}
