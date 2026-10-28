// Author: Phan Tuan Thanh - CE190595
import { useEffect, useState } from 'react'
import { AuthContext } from './AuthContext'
import { clearSession, readSession, saveSession } from './session'

export default function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession)
  const [notice, setNotice] = useState('')

  useEffect(() => {
    function expireSession() {
      clearSession()
      setSession(null)
      setNotice('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    }

    window.addEventListener('auth:expired', expireSession)
    let timer
    if (session) {
      const remainingTime = Date.parse(session.expiresAt) - Date.now()
      timer = window.setTimeout(expireSession, Math.max(0, remainingTime))
    }
    return () => {
      window.removeEventListener('auth:expired', expireSession)
      window.clearTimeout(timer)
    }
  }, [session])

  function startSession(response) {
    saveSession(response)
    setSession(response)
    setNotice('')
  }

  function logout() {
    clearSession()
    setSession(null)
    setNotice('')
  }

  return (
    <AuthContext.Provider value={{ session, notice, startSession, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
