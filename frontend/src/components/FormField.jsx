export default function FormField({ id, label, hint, error, children }) {
  return (
    <div>
      <label className="label" htmlFor={id}>
        {label}
      </label>
      {children}
      {hint && (
        <p className="mt-1 text-xs text-slate-500" id={`${id}-hint`}>
          {hint}
        </p>
      )}
      {error && (
        <p className="mt-1 text-xs text-rose-600" id={`${id}-error`} role="alert">
          {error}
        </p>
      )}
    </div>
  )
}
