export default function StatCard({ label, value, tone = '#1b73e8' }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
      <p className="text-xs font-medium uppercase tracking-wide text-slate-400">{label}</p>
      <p className="mt-1 text-2xl font-bold" style={{ color: tone }}>
        {value ?? '-'}
      </p>
    </div>
  )
}