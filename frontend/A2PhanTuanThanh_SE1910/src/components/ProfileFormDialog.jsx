// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'

export default function ProfileFormDialog({ profile, onClose, onSaved }) {
  const dialogRef = useRef(null)
  const [accountName, setAccountName] = useState(profile.accountName)
  const [accountEmail, setAccountEmail] = useState(profile.accountEmail)
  const [password, setPassword] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  useEffect(() => { dialogRef.current.showModal() }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    if (saving) return
    const errors = {}
    if (!accountName.trim()) errors.accountName = 'Please enter a name.'
    if (!accountEmail.trim()) {
      errors.accountEmail = 'Please enter an email.'
    } else if (event.currentTarget.elements.accountEmail.validity.typeMismatch) {
      errors.accountEmail = 'Invalid email format.'
    }
    if (password !== '') {
      if (!password.trim() || password.length < 8) {
        errors.password = 'Use at least 8 characters; password cannot contain only whitespace.'
      } else if (new TextEncoder().encode(password).length > 72) {
        errors.password = 'Password must be at most 72 UTF-8 bytes.'
      }
    }
    setFieldErrors(errors)
    setError('')
    if (Object.keys(errors).length > 0) return

    setSaving(true)
    try {
      const body = { accountName: accountName.trim(), accountEmail: accountEmail.trim() }
      if (password !== '') body.password = password
      const response = await client.put('/profile', body)
      onSaved(response.data)
    } catch (requestError) {
      if (requestError.response?.status === 409) {
        setError('Email is already in use. Please choose another email.')
      } else {
        setError(requestError.response?.data?.message || 'Unable to save your profile. Please try again.')
      }
      setFieldErrors(requestError.response?.data?.fieldErrors || {})
    } finally {
      setSaving(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog" aria-labelledby="profile-form-title"
      onCancel={(event) => { event.preventDefault(); if (!saving) onClose() }}>
      <form onSubmit={handleSubmit} noValidate aria-busy={saving}>
        <h2 id="profile-form-title">Edit profile</h2>
        {error && <p className="error-message" role="alert">{error}</p>}
        <fieldset disabled={saving}>
          <label htmlFor="profile-name">Name</label>
          <input id="profile-name" required maxLength={100} value={accountName}
            onChange={(event) => setAccountName(event.target.value)} aria-invalid={Boolean(fieldErrors.accountName)}
            aria-describedby={fieldErrors.accountName ? 'profile-name-error' : undefined} />
          {fieldErrors.accountName && <p id="profile-name-error" className="field-error" role="alert">{fieldErrors.accountName}</p>}
          <label htmlFor="profile-email">Email</label>
          <input id="profile-email" name="accountEmail" type="email" required maxLength={254} value={accountEmail}
            onChange={(event) => setAccountEmail(event.target.value)} aria-invalid={Boolean(fieldErrors.accountEmail)}
            aria-describedby={fieldErrors.accountEmail ? 'profile-email-error' : undefined} />
          {fieldErrors.accountEmail && <p id="profile-email-error" className="field-error" role="alert">{fieldErrors.accountEmail}</p>}
          <label htmlFor="profile-password">New password</label>
          <input id="profile-password" type="password" autoComplete="new-password" maxLength={72} value={password}
            onChange={(event) => setPassword(event.target.value)} aria-invalid={Boolean(fieldErrors.password)}
            aria-describedby={fieldErrors.password ? 'profile-password-error' : 'profile-password-hint'} />
          <p id="profile-password-hint" className="muted field-hint">
            Leave blank to keep your current password. New passwords need at least 8 characters and at most 72 UTF-8 bytes.
          </p>
          {fieldErrors.password && <p id="profile-password-error" className="field-error" role="alert">{fieldErrors.password}</p>}
        </fieldset>
        <div className="dialog-actions">
          <button type="button" className="button-secondary" onClick={onClose} disabled={saving}>Cancel</button>
          <button type="submit" className="button-primary" disabled={saving}>{saving ? 'Saving…' : 'Save profile'}</button>
        </div>
      </form>
    </dialog>
  )
}
