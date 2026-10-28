// Author: Phan Tuan Thanh - CE190595
import { useEffect, useRef, useState } from 'react'
import client from '../api/client'

export default function AccountFormDialog({ onClose, onSaved }) {
  const dialogRef = useRef(null)
  const [accountName, setAccountName] = useState('')
  const [accountEmail, setAccountEmail] = useState('')
  const [accountRole, setAccountRole] = useState('2')
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
    if (!accountName.trim()) errors.accountName = 'Vui lòng nhập tên.'
    if (!accountEmail.trim()) {
      errors.accountEmail = 'Vui lòng nhập email.'
    } else if (event.currentTarget.elements.accountEmail.validity.typeMismatch) {
      errors.accountEmail = 'Email không đúng định dạng.'
    }
    if (!password.trim()) {
      errors.password = 'Vui lòng nhập mật khẩu.'
    } else if (new TextEncoder().encode(password).length > 72) {
      errors.password = 'Mật khẩu tối đa 72 byte UTF-8.'
    }
    setFieldErrors(errors)
    setError('')
    if (Object.keys(errors).length > 0) return

    setSaving(true)
    try {
      await client.post('/accounts', {
        accountName: accountName.trim(), accountEmail: accountEmail.trim(),
        accountRole: Number(accountRole), password,
      })
      onSaved()
    } catch (requestError) {
      if (requestError.response?.status === 409) {
        setError('Không lưu được tài khoản. Email có thể đã được sử dụng; vui lòng kiểm tra lại.')
      } else {
        setError(requestError.response?.data?.message || 'Không lưu được tài khoản. Vui lòng thử lại.')
      }
      setFieldErrors(requestError.response?.data?.fieldErrors || {})
    } finally {
      setSaving(false)
    }
  }

  return (
    <dialog ref={dialogRef} className="account-dialog" aria-labelledby="account-form-title" onCancel={handleCancel}>
      <form onSubmit={handleSubmit} noValidate aria-busy={saving}>
        <h2 id="account-form-title">Thêm tài khoản</h2>
        {error && <p className="error-message" role="alert">{error}</p>}
        <fieldset disabled={saving}>
          <label htmlFor="account-name">Tên</label>
          <input id="account-name" name="accountName" required maxLength={100} value={accountName}
            onChange={(event) => setAccountName(event.target.value)} aria-invalid={Boolean(fieldErrors.accountName)}
            aria-describedby={fieldErrors.accountName ? 'account-name-error' : undefined} />
          {fieldErrors.accountName && <p id="account-name-error" className="field-error" role="alert">{fieldErrors.accountName}</p>}
          <label htmlFor="account-email">Email</label>
          <input id="account-email" name="accountEmail" type="email" required maxLength={254} value={accountEmail}
            onChange={(event) => setAccountEmail(event.target.value)} aria-invalid={Boolean(fieldErrors.accountEmail)}
            aria-describedby={fieldErrors.accountEmail ? 'account-email-error' : undefined} />
          {fieldErrors.accountEmail && <p id="account-email-error" className="field-error" role="alert">{fieldErrors.accountEmail}</p>}
          <label htmlFor="account-role">Vai trò</label>
          <select id="account-role" value={accountRole} onChange={(event) => setAccountRole(event.target.value)}
            aria-invalid={Boolean(fieldErrors.accountRole)} aria-describedby={fieldErrors.accountRole ? 'account-role-error' : undefined}>
            <option value="1">Admin</option><option value="2">Staff</option>
          </select>
          {fieldErrors.accountRole && <p id="account-role-error" className="field-error" role="alert">{fieldErrors.accountRole}</p>}
          <label htmlFor="account-password">Mật khẩu</label>
          <input id="account-password" type="password" autoComplete="new-password" required maxLength={72}
            value={password} onChange={(event) => setPassword(event.target.value)} aria-invalid={Boolean(fieldErrors.password)}
            aria-describedby={fieldErrors.password ? 'account-password-error' : undefined} />
          {fieldErrors.password && <p id="account-password-error" className="field-error" role="alert">{fieldErrors.password}</p>}
        </fieldset>
        <div className="dialog-actions">
          <button type="button" className="button-secondary" onClick={onClose} disabled={saving}>Hủy</button>
          <button type="submit" className="button-primary" disabled={saving}>{saving ? 'Đang lưu…' : 'Lưu tài khoản'}</button>
        </div>
      </form>
    </dialog>
  )
}
