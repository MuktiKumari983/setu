import test from 'node:test';
import assert from 'node:assert/strict';
import { aiSourceLabel, asList, isCanonicalEntityType, toNumberOrZero } from './contract.js';

test('asList handles comma separated backend strings', () => {
  assert.deepEqual(asList('IoT, GIS, Web Development'), ['IoT', 'GIS', 'Web Development']);
});

test('asList handles arrays and removes blank items', () => {
  assert.deepEqual(asList(['IoT', '', null, ' GIS ']), ['IoT', 'GIS']);
});

test('asList returns an empty array for null and numbers', () => {
  assert.deepEqual(asList(null), []);
  assert.deepEqual(asList(42), []);
});

test('toNumberOrZero converts valid values and rejects invalid ones', () => {
  assert.equal(toNumberOrZero('12'), 12);
  assert.equal(toNumberOrZero(''), 0);
  assert.equal(toNumberOrZero('abc'), 0);
});

test('AI entity types use the canonical Java/Python vocabulary', () => {
  assert.equal(isCanonicalEntityType('UNIVERSITY'), true);
  assert.equal(isCanonicalEntityType('TEAM'), true);
  assert.equal(isCanonicalEntityType('STUDENT_TEAM'), false);
  assert.equal(isCanonicalEntityType('team'), false);
});

test('AI source labels expose remote, fallback, and local results', () => {
  assert.equal(aiSourceLabel('REMOTE'), 'Remote AI service');
  assert.equal(aiSourceLabel('FALLBACK'), 'Local AI fallback');
  assert.equal(aiSourceLabel('LOCAL'), 'Local AI rules');
  assert.equal(aiSourceLabel('OTHER'), 'Unknown AI source');
});
