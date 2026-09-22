import { useEffect, useState } from 'react'
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
  Legend,
  LineChart,
  Line,
} from 'recharts'
import { dashboardApi } from '../api/client'
import StatCard from '../components/StatCard'
import { errorMessage } from '../util'

const CATEGORY_COLORS = ['#1b73e8', '#16a34a', '#f59e0b', '#dc2626', '#6366f1', '#0d9488', '#8b5cf6', '#ea580c', '#0891b2', '#be185d', '#65a30d']

export default function AnalyticsPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    dashboardApi
      .analytics()
      .then(setData)
      .catch((e) => setError(errorMessage(e)))
  }, [])

  if (error) return <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>
  if (!data) return <p className="text-sm text-slate-500">Loading analytics...</p>

  const byCategory = Object.entries(data.byCategory || {}).map(([name, value]) => ({ name, value }))
  const byStatus = Object.entries(data.byStatus || {}).map(([name, value]) => ({ name, value }))
  const projectsByStatus = Object.entries(data.projectsByStatus || {}).map(([name, value]) => ({ name, value }))
  const universityParticipation = Object.entries(data.universityParticipation || {}).map(([name, value]) => ({ name, value }))

  return (
    <div className="space-y-5">
      <h1 className="text-lg font-semibold text-slate-800">Platform Analytics</h1>

      <div className="grid grid-cols-2 gap-3 md:grid-cols-4 lg:grid-cols-6">
        <StatCard label="Challenges" value={data.totalChallenges} />
        <StatCard label="Submitted" value={data.submittedChallenges} />
        <StatCard label="Verified" value={data.verifiedChallenges} tone="#0ea5e9" />
        <StatCard label="Merged" value={data.mergedChallenges} tone="#475569" />
        <StatCard label="Active projects" value={data.activeProjects} tone="#059669" />
        <StatCard label="Needs support" value={data.projectsNeedingSupport} tone="#dc2626" />
        <StatCard label="Matches produced" value={data.matchCount} tone="#6366f1" />
        <StatCard label="Failed pilots" value={data.failedPilots} tone="#ea580c" />
        <StatCard label="Universities" value={data.universityCount} />
        <StatCard label="Industries" value={data.industryCount} />
      </div>

      <div className="grid gap-5 lg:grid-cols-2">
        <div className="card">
          <h3 className="mb-3 text-sm font-semibold text-slate-700">Challenges by category</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={byCategory}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="name" tick={{ fontSize: 10 }} interval={0} angle={-25} textAnchor="end" height={70} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="value" fill="#1b73e8" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <h3 className="mb-3 text-sm font-semibold text-slate-700">Challenges by lifecycle status</h3>
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie data={byStatus} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label={(e) => `${e.name}:${e.value}`} labelLine={false}>
                {byStatus.map((entry, i) => (
                  <Cell key={entry.name} fill={CATEGORY_COLORS[i % CATEGORY_COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <h3 className="mb-3 text-sm font-semibold text-slate-700">Projects by stage (pipeline)</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={projectsByStatus}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="name" tick={{ fontSize: 10 }} interval={0} angle={-25} textAnchor="end" height={70} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="value" fill="#16a34a" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <h3 className="mb-3 text-sm font-semibold text-slate-700">University participation</h3>
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={universityParticipation}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="name" tick={{ fontSize: 10 }} interval={0} angle={-25} textAnchor="end" height={70} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
              <Tooltip />
              <Line type="monotone" dataKey="value" stroke="#6366f1" strokeWidth={2} dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card">
        <h3 className="mb-2 text-sm font-semibold text-slate-700">Published impact to date</h3>
        <p className="text-sm text-slate-500">
          {data.impactSummary?.verifiedRecords} of {data.impactSummary?.recordCount} records verified, reaching{' '}
          <strong>{data.impactSummary?.verifiedBeneficiaries}</strong> verified beneficiaries.
        </p>
      </div>
    </div>
  )
}