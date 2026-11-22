import { useEffect, useRef } from 'react'
import { Routes, Route, useLocation } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LandingPage from './pages/LandingPage'
import NotFoundPage from './pages/NotFoundPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import ChallengesPage from './pages/ChallengesPage'
import ChallengeDetailPage from './pages/ChallengeDetailPage'
import ProjectsPage from './pages/ProjectsPage'
import ProjectCreatePage from './pages/ProjectCreatePage'
import ProjectDetailPage from './pages/ProjectDetailPage'
import CapabilitiesPage from './pages/CapabilitiesPage'
import CatalogPage from './pages/CatalogPage'
import MatchingPage from './pages/MatchingPage'
import ImpactPage from './pages/ImpactPage'
import AnalyticsPage from './pages/AnalyticsPage'
import ProfilePage from './pages/ProfilePage'

function RouteFocus() {
  const { pathname } = useLocation()
  const firstRender = useRef(true)

  useEffect(() => {
    if (firstRender.current) {
      firstRender.current = false
      return
    }
    document.getElementById('main-content')?.focus()
  }, [pathname])

  return null
}

export default function App() {
  return (
    <>
      <RouteFocus />
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/challenges" element={<ChallengesPage />} />
          <Route path="/challenges/:id" element={<ChallengeDetailPage />} />
          <Route path="/projects" element={<ProjectsPage />} />
          <Route path="/projects/new" element={<ProjectCreatePage />} />
          <Route path="/projects/:id" element={<ProjectDetailPage />} />
          <Route path="/capabilities" element={<CapabilitiesPage />} />
          <Route path="/catalog" element={<CatalogPage />} />
          <Route path="/matching" element={<MatchingPage />} />
          <Route path="/matching/:id" element={<MatchingPage />} />
          <Route path="/impact" element={<ImpactPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </>
  )
}
