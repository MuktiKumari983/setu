import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import ErrorBanner from './ErrorBanner'

describe('ErrorBanner', () => {
  it('renders the message with an alert role', () => {
    render(<ErrorBanner message="Unable to load challenges" />)

    expect(screen.getByRole('alert')).toHaveTextContent('Unable to load challenges')
  })

  it('renders nothing without a message', () => {
    const { container } = render(<ErrorBanner />)

    expect(container).toBeEmptyDOMElement()
  })
})
