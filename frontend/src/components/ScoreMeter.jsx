export default function ScoreMeter({ score }) {
  const pct = Math.round((score || 0) * 100)
  const color = pct >= 75 ? '#16a34a' : pct >= 45 ? '#f59e0b' : '#dc2626'
  return (
    <div className="flex items-center gap-2">
      <div className="h-2 w-24 overflow-hidden rounded-full bg-slate-200">
        <div className="h-full rounded-full" style={{ width: `${pct}%`, backgroundColor: color }} />
      </div>
      <span className="text-xs font-semibold" style={{ color }}>
        {pct}%
      </span>
    </div>
  )
}