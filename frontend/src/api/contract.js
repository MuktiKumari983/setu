export const AI_ENTITY_TYPES = ['UNIVERSITY', 'TEAM', 'MENTOR', 'INDUSTRY'];

export const AI_SOURCE_LABELS = {
  REMOTE: 'Remote AI service',
  FALLBACK: 'Local AI fallback',
  LOCAL: 'Local AI rules',
};

export function asList(value) {
  if (Array.isArray(value)) {
    return value
      .filter((item) => item !== null && item !== undefined && String(item).trim() !== '')
      .map((item) => String(item).trim());
  }
  if (typeof value === 'string') {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }
  return [];
}

export function toNumberOrZero(value) {
  if (value === null || value === undefined || value === '') return 0;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function isCanonicalEntityType(value) {
  return AI_ENTITY_TYPES.includes(value);
}

export function aiSourceLabel(value) {
  return AI_SOURCE_LABELS[value] || 'Unknown AI source';
}
