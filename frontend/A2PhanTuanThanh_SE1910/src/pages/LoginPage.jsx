// Author: Phan Tuan Thanh - CE190595
import { useContext, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
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
      setFieldErrors({ password: 'Password must be at most 72 UTF-8 bytes.' })
      return
    }
    setSubmitting(true)
    try {
      const response = await client.post('/auth/login', { email: email.trim(), password })
      startSession(response.data)
    } catch (requestError) {
      if (requestError.response?.status === 401) {
        setError('Incorrect email or password.')
      } else if (requestError.response?.status === 400) {
        setFieldErrors(requestError.response.data.fieldErrors || {})
        setError(requestError.response.data.message || 'Please check your sign-in details.')
      } else {
        setError('Unable to sign in. Please check your connection and try again.')
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
          <h1>Stay informed.<br />Manage with ease.</h1>
          <p>Sign in to access your workspace.</p>
        </div>
        <small>SBA301 · Assignment 02</small>
      </section>
      <section className="login-panel" aria-labelledby="login-title">
        <p className="eyebrow">Welcome back</p>
        <h2 id="login-title">Sign in</h2>
        <p className="muted">Enter your email and password.</p>
        <p><Link to="/public/news">Read public news</Link></p>
        {notice && <p className="notice" role="status">{notice}</p>}
        {error && <p className="error-message" role="alert">{error}</p>}
        <form onSubmit={handleSubmit} aria-busy={submitting}>
          <label htmlFor="email">Email</label>
          <input id="email" type="email" autoComplete="username" required maxLength={254}
            value={email} onChange={(event) => setEmail(event.target.value)} disabled={submitting}
            aria-invalid={Boolean(fieldErrors.email)} aria-describedby={fieldErrors.email ? 'email-error' : undefined} />
          {fieldErrors.email && <p id="email-error" className="field-error">{fieldErrors.email}</p>}
          <label htmlFor="password">Password</label>
          <input id="password" type="password" autoComplete="current-password" required maxLength={72}
            value={password} onChange={(event) => setPassword(event.target.value)} disabled={submitting}
            aria-invalid={Boolean(fieldErrors.password)} aria-describedby={fieldErrors.password ? 'password-error' : undefined} />
          {fieldErrors.password && <p id="password-error" className="field-error">{fieldErrors.password}</p>}
          <button type="submit" className="button-primary" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
      </section>
    </main>
  )
}
