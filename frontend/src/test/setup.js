if (process.env.VITEST) {
  await import('@testing-library/jest-dom/vitest')
  const { cleanup } = await import('@testing-library/react')
  const { afterEach } = await import('vitest')

  afterEach(cleanup)
}
