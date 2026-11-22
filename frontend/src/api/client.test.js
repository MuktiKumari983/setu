import test from 'node:test';
import assert from 'node:assert/strict';
import { buildApiUrl } from './client.js';

test('empty base leaves API path unchanged', () => {
  assert.equal(buildApiUrl('', '/api/auth/login'), '/api/auth/login');
});

test('api base does not duplicate the /api prefix', () => {
  assert.equal(buildApiUrl('/api', '/api/auth/login'), '/api/auth/login');
});

test('origin base is joined with the API path', () => {
  assert.equal(buildApiUrl('https://example.test', '/api/auth/login'), 'https://example.test/api/auth/login');
});

test('origin base ending in /api is not duplicated', () => {
  assert.equal(buildApiUrl('https://example.test/api', '/api/auth/login'), 'https://example.test/api/auth/login');
});

test('absolute URLs are left unchanged', () => {
  assert.equal(buildApiUrl('/api', 'https://example.test/health'), 'https://example.test/health');
});
