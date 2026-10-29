// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import client from '../api/client'

export default function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    async function loadProfile() {
      try {
        const response = await client.get('/profile', { signal: controller.signal })
        if (!controller.signal.aborted) setProfile(response.data)
      } catch (requestError) {
        if (!controller.signal.aborted) {
          if (requestError.response?.status === 403) {
            setError('You do not have permission to view this profile.')
          } else {
            setError('Unable to load your profile. Please try again.')
          }
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }
    loadProfile()
    return () => controller.abort()
  }, [reloadCount])

  function reloadProfile() {
    setLoading(true)
    setError('')
    setReloadCount((count) => count + 1)
  }

  return (
    <>
      <p className="eyebrow">My account</p>
      <h1>My profile</h1>
      <p className="muted page-description">View your current account details.</p>
      <section className="card" aria-labelledby="profile-title" aria-busy={loading}>
        <div className="card-heading"><h2 id="profile-title">Account details</h2></div>
        {loading && <p className="state-message" role="status">Loading your profile…</p>}
        {!loading && error && <div className="state-message">
          <p className="error-message" role="alert">{error}</p>
          <button type="button" className="button-secondary" onClick={reloadProfile}>Try again</button>
        </div>}
        {!loading && !error && profile && <dl className="profile-details">
          <dt>Account ID</dt><dd>{profile.accountId}</dd>
          <dt>Name</dt><dd>{profile.accountName}</dd>
          <dt>Email</dt><dd>{profile.accountEmail}</dd>
          <dt>Role</dt><dd><span className="role-badge staff">
            {profile.accountRole === 2 ? 'Staff' : 'Admin'}
          </span></dd>
        </dl>}
      </section>
    </>
  )
}
