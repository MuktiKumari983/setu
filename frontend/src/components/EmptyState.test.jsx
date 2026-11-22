import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import EmptyState from './EmptyState'

describe('EmptyState', () => {
  it('renders the title, description, and action', () => {
    render(
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <EmptyState
          title="No projects yet"
          description="Create a project to get started."
          to="/projects/new"
          actionLabel="New project"
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('No projects yet')).toBeInTheDocument()
    expect(screen.getByText('Create a project to get started.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'New project' })).toHaveAttribute('href', '/projects/new')
  })

  it('omits the action when the destination or label is missing', () => {
    render(
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <EmptyState title="Nothing here" />
      </MemoryRouter>,
    )

    expect(screen.getByText('Nothing here')).toBeInTheDocument()
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
  })
})
