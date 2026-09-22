import { createContext, useContext, useEffect, useState } from 'react'
import { api, setToken, authApi } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('sih_token')
    if (!token) {
      setLoading(false)
      return
    }
    api('/auth/me')
      .then((res) => setUser(res.user || null))
      .catch(() => {
        setToken(null)
      })
      .finally(() => setLoading(false))
  }, [])

  async function login(email, password) {
    const res = await authApi.login(email, password)
    setToken(res.token)
    setUser(res.user)
    return res.user
  }

  async function register(payload) {
    const res = await authApi.register(payload)
    setToken(res.token)
    setUser(res.user)
    return res.user
  }

  function logout() {
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, setUser, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}