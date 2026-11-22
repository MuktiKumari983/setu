const ALL_CAPABILITIES = [
  'viewDashboard',
  'viewChallenges',
  'viewProjects',
  'viewMatching',
  'viewImpact',
  'viewCatalog',
  'viewCapabilities',
  'viewAnalytics',
  'submitChallenges',
  'createProjects',
  'manageProjects',
  'reviewImpact',
]

const ROLE_CAPABILITIES = {
  CITIZEN: [
    'viewDashboard',
    'viewChallenges',
    'viewProjects',
    'viewMatching',
    'viewImpact',
    'viewCatalog',
    'viewCapabilities',
    'submitChallenges',
  ],
  STUDENT: [
    'viewDashboard',
    'viewChallenges',
    'viewProjects',
    'viewMatching',
    'viewImpact',
    'viewCatalog',
    'viewCapabilities',
    'submitChallenges',
    'createProjects',
    'manageProjects',
  ],
  FACULTY: [
    'viewDashboard',
    'viewChallenges',
    'viewProjects',
    'viewMatching',
    'viewImpact',
    'viewCatalog',
    'viewCapabilities',
    'viewAnalytics',
    'createProjects',
    'manageProjects',
  ],
  UNIVERSITY_ADMIN: [
    'viewDashboard',
    'viewChallenges',
    'viewProjects',
    'viewMatching',
    'viewImpact',
    'viewCatalog',
    'viewCapabilities',
    'viewAnalytics',
    'createProjects',
    'manageProjects',
  ],
  INDUSTRY: [
    'viewDashboard',
    'viewProjects',
    'viewMatching',
    'viewImpact',
    'viewCatalog',
    'viewCapabilities',
  ],
  GOVERNMENT_ADMIN: ALL_CAPABILITIES,
  PLATFORM_ADMIN: ALL_CAPABILITIES,
}

export const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', capability: 'viewDashboard' },
  { to: '/challenges', label: 'Challenges', capability: 'viewChallenges' },
  { to: '/projects', label: 'Projects', capability: 'viewProjects' },
  { to: '/matching', label: 'Matching', capability: 'viewMatching' },
  { to: '/impact', label: 'Impact', capability: 'viewImpact' },
  { to: '/capabilities', label: 'Capabilities', capability: 'viewCapabilities' },
  { to: '/catalog', label: 'Catalog', capability: 'viewCatalog' },
  { to: '/analytics', label: 'Analytics', capability: 'viewAnalytics' },
]

export function can(user, capability) {
  if (!user?.role) return false
  return (ROLE_CAPABILITIES[user.role] || []).includes(capability)
}

export function visibleNavItems(user) {
  return NAV_ITEMS.filter((item) => can(user, item.capability))
}

export function roleLabel(role) {
  if (!role) return 'Member'
  return role
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}
