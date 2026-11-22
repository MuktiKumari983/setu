import { Link } from 'react-router-dom'

export default function EmptyState({ title, description, to, actionLabel }) {
  return (
    <div className="rounded-lg border border-dashed border-slate-300 p-6 text-center">
      <p className="text-sm font-medium text-slate-700">{title}</p>
      {description && <p className="mt-1 text-xs text-slate-500">{description}</p>}
      {to && actionLabel && (
        <Link to={to} className="mt-3 inline-block text-sm font-medium text-brand-600 hover:underline">
          {actionLabel}
        </Link>
      )}
    </div>
  )
}
