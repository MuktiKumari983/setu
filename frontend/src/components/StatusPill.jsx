const COLORS = {
  SUBMITTED: '#64748b',
  VERIFIED: '#0ea5e9',
  MATCHED: '#6366f1',
  TEAM_FORMED: '#8b5cf6',
  PROPOSAL: '#0d9488',
  PROTOTYPE: '#f59e0b',
  REVIEW: '#f97316',
  PILOT: '#059669',
  IMPLEMENTATION: '#15803d',
  IMPACT: '#16a34a',
  COMPLETED: '#166534',
  NEEDS_SUPPORT: '#dc2626',
  CANCELLED: '#000000',
  MERGED: '#475569',
  DONE: '#16a34a',
  OPEN: '#64748b',
  IN_PROGRESS: '#f59e0b',
  PENDING: '#64748b',
  HIGH: '#dc2626',
  MEDIUM: '#f59e0b',
  LOW: '#16a34a',
  PROPOSED: '#6366f1',
  APPROVED: '#16a34a',
  REJECTED: '#dc2626',
  PENDING_REVIEW: '#f59e0b',
  VERIFIED_RECORD: '#16a34a',
  VERIFIED: '#16a34a',
}

export default function StatusPill({ value }) {
  if (!value) return <span className="text-xs text-slate-400">-</span>
  const color = COLORS[String(value).toUpperCase()] || '#475569'
  return (
    <span
      className="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold"
      style={{ backgroundColor: `${color}1a`, color }}
    >
      {value}
    </span>
  )
}