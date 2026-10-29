// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'

export default function AccountFormDialog({ account, onClose, onSaved }) {
  const dialogRef = useRef(null)
  const isEditing = Boolean(account)
  const [accountName, setAccountName] = useState(account?.accountName || '')
  const [accountEmail, setAccountEmail] = useState(account?.accountEmail || '')
  const [accountRole, setAccountRole] = useState(String(account?.accountRole || 2))
  const [password, setPassword] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  useEffect(() => {
    dialogRef.current.showModal()
  }, [])

  function handleCancel(event) {
    event.preventDefault()
    if (!saving) onClose()
  }

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
    if (!isEditing || password !== '') {
      if (!password.trim()) {
        errors.password = 'Password cannot contain only whitespace.'
      } else if (new TextEncoder().encode(password).length > 72) {
        errors.password = 'Password must be at most 72 UTF-8 bytes.'
      }
    }
    setFieldErrors(errors)
    setError('')
    if (Object.keys(errors).length > 0) return

    setSaving(true)
    try {
      const body = {
        accountName: accountName.trim(), accountEmail: accountEmail.trim(),
        accountRole: Number(accountRole),
      }
      if (!isEditing || password !== '') body.password = password
      let response
      if (isEditing) {
        response = await client.put(`/accounts/${account.accountId}`, body)
      } else {
        response = await client.post('/accounts', body)
      }
      onSaved(response.data)
    } catch (requestError) {
      if (requestError.response?.status === 409) {
        setError('Unable to save the account. The email may already be in use; please check and try again.')
      } else {
        setError(requestError.response?.data?.message || 'Unable to save the account. Please try again.')
      }
      setFieldErrors(requestError.response?.data?.fieldErrors || {})
    } finally {
      setSaving(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog" aria-labelledby="account-form-title" onCancel={handleCancel}>
      <form onSubmit={handleSubmit} noValidate aria-busy={saving}>
        <h2 id="account-form-title">{isEditing ? 'Edit account' : 'Add account'}</h2>
        {error && <p className="error-message" role="alert">{error}</p>}
        <fieldset disabled={saving}>
          <label htmlFor="account-name">Name</label>
          <input id="account-name" name="accountName" required maxLength={100} value={accountName}
            onChange={(event) => setAccountName(event.target.value)} aria-invalid={Boolean(fieldErrors.accountName)}
            aria-describedby={fieldErrors.accountName ? 'account-name-error' : undefined} />
          {fieldErrors.accountName && <p id="account-name-error" className="field-error" role="alert">{fieldErrors.accountName}</p>}
          <label htmlFor="account-email">Email</label>
          <input id="account-email" name="accountEmail" type="email" required maxLength={254} value={accountEmail}
            onChange={(event) => setAccountEmail(event.target.value)} aria-invalid={Boolean(fieldErrors.accountEmail)}
            aria-describedby={fieldErrors.accountEmail ? 'account-email-error' : undefined} />
          {fieldErrors.accountEmail && <p id="account-email-error" className="field-error" role="alert">{fieldErrors.accountEmail}</p>}
          <label htmlFor="account-role">Role</label>
          <select id="account-role" value={accountRole} onChange={(event) => setAccountRole(event.target.value)}
            aria-invalid={Boolean(fieldErrors.accountRole)} aria-describedby={fieldErrors.accountRole ? 'account-role-error' : undefined}>
            <option value="1">Admin</option><option value="2">Staff</option>
          </select>
          {fieldErrors.accountRole && <p id="account-role-error" className="field-error" role="alert">{fieldErrors.accountRole}</p>}
          <label htmlFor="account-password">{isEditing ? 'New password' : 'Password'}</label>
          <input id="account-password" type="password" autoComplete="new-password" required={!isEditing} maxLength={72}
            value={password} onChange={(event) => setPassword(event.target.value)} aria-invalid={Boolean(fieldErrors.password)}
            aria-describedby={fieldErrors.password ? 'account-password-error' : 'account-password-hint'} />
          <p id="account-password-hint" className="muted field-hint">
            {isEditing ? 'Leave blank to keep the current password.' : 'Password must be at most 72 UTF-8 bytes.'}
          </p>
          {fieldErrors.password && <p id="account-password-error" className="field-error" role="alert">{fieldErrors.password}</p>}
        </fieldset>
        <div className="dialog-actions">
          <button type="button" className="button-secondary" onClick={onClose} disabled={saving}>Cancel</button>
          <button type="submit" className="button-primary" disabled={saving}>{saving ? 'Saving…' : 'Save account'}</button>
        </div>
      </form>
    </dialog>
  )
}
