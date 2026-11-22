import test from 'node:test'
import assert from 'node:assert/strict'
import { can, visibleNavItems, roleLabel } from './roles.js'

test('role capabilities match backend roles', () => {
  assert.equal(can({ role: 'CITIZEN' }, 'submitChallenges'), true)
  assert.equal(can({ role: 'CITIZEN' }, 'createProjects'), false)
  assert.equal(can({ role: 'STUDENT' }, 'createProjects'), true)
  assert.equal(can({ role: 'GOVERNMENT_ADMIN' }, 'reviewImpact'), true)
  assert.equal(can({ role: 'PLATFORM_ADMIN' }, 'viewAnalytics'), true)
  assert.equal(can(null, 'viewDashboard'), false)
})

test('navigation is role aware', () => {
  const citizen = visibleNavItems({ role: 'CITIZEN' }).map((item) => item.to)
  const industry = visibleNavItems({ role: 'INDUSTRY' }).map((item) => item.to)
  const admin = visibleNavItems({ role: 'PLATFORM_ADMIN' }).map((item) => item.to)

  assert.ok(citizen.includes('/challenges'))
  assert.ok(!citizen.includes('/analytics'))
  assert.ok(!industry.includes('/challenges'))
  assert.ok(industry.includes('/catalog'))
  assert.equal(admin.length, 8)
})

test('role labels are readable', () => {
  assert.equal(roleLabel('UNIVERSITY_ADMIN'), 'University Admin')
  assert.equal(roleLabel(null), 'Member')
})
