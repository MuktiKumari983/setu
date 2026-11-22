export default function SuccessBanner({ message }) {
  if (!message) return null
  return (
    <div className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700" role="status" aria-live="polite">
      {message}
    </div>
  )
}
