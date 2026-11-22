import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import FormField from './FormField'

describe('FormField', () => {
  it('associates the label with the input and renders hint and error text', () => {
    render(
      <FormField id="email" label="Email" hint="We never share it." error="Email is required">
        <input id="email" />
      </FormField>,
    )

    expect(screen.getByText('Email')).toHaveAttribute('for', 'email')
    expect(screen.getByLabelText('Email')).toHaveAttribute('id', 'email')
    expect(screen.getByText('We never share it.')).toHaveAttribute('id', 'email-hint')
    expect(screen.getByRole('alert')).toHaveTextContent('Email is required')
  })

  it('omits the error alert when no error is supplied', () => {
    render(
      <FormField id="name" label="Name">
        <input id="name" />
      </FormField>,
    )

    expect(screen.getByLabelText('Name')).toHaveAttribute('id', 'name')
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })
})
