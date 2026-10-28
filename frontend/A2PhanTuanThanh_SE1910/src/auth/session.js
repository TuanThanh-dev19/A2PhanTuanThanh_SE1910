// Author: Phan Tuan Thanh - CE190595
const SESSION_KEY = 'funews.session'

export function readSession() {
  try {
    const session = JSON.parse(sessionStorage.getItem(SESSION_KEY))
    const account = session?.account
    const expiresAt = Date.parse(session?.expiresAt)
    if (typeof session?.accessToken === 'string' && session.accessToken.length > 0
        && session.tokenType === 'Bearer' && Number.isFinite(expiresAt) && expiresAt > Date.now()
        && Number.isInteger(account?.accountId) && typeof account.accountName === 'string'
        && typeof account.accountEmail === 'string' && [1, 2].includes(account.accountRole)) {
      return session
    }
  } catch {
    // Invalid local data must never prevent the login screen from opening.
  }
  sessionStorage.removeItem(SESSION_KEY)
  return null
}

export function saveSession(session) {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession() {
  sessionStorage.removeItem(SESSION_KEY)
}

export function homePath(session) {
  if (!session) return '/login'
  if (session.account.accountRole === 1) return '/accounts'
  return '/staff'
}
