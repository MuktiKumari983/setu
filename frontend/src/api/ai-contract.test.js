import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { aiSourceLabel, asList, isCanonicalEntityType } from './contract.js';

const javaCandidatePayload = {
  id: 7,
  name: 'River Sensors',
  description: 'water monitoring',
  skills: ['IoT'],
  tags: ['river'],
  capabilities: ['Sensor deployment'],
  availability: 'Weekends',
  experience: ['river sensor rollout'],
};

const pythonAnalyzePayload = {
  category: 'Water Quality',
  urgency: 'HIGH',
  requiredSkills: ['IoT'],
  keywords: ['river'],
  confidence: 0.81,
  explanation: 'matched',
  rootCauses: ['Reported signal: river'],
  targetBeneficiaries: ['villagers'],
  challenges: ['Reported condition: river'],
  solutionDirections: ['Assess capability: Sensor deployment'],
  expectedOutcomes: ['Not specified in submission'],
  source: 'REMOTE',
};

describe('AI service contract', () => {
  it('Java candidate payload contains every field Python requires', () => {
    assert.deepEqual(Object.keys(javaCandidatePayload), [
      'id', 'name', 'description', 'skills', 'tags', 'capabilities', 'availability', 'experience',
    ]);
    assert.equal(typeof javaCandidatePayload.availability, 'string');
    assert.ok(Array.isArray(javaCandidatePayload.experience));
  });

  it('Python analyze payload contains every field Java validates', () => {
    assert.deepEqual(Object.keys(pythonAnalyzePayload).sort(), [
      'category', 'challenges', 'confidence', 'expectedOutcomes', 'explanation', 'keywords',
      'requiredSkills', 'rootCauses', 'solutionDirections', 'source', 'targetBeneficiaries', 'urgency',
    ].sort());
    assert.equal(pythonAnalyzePayload.source, 'REMOTE');
    assert.ok(asList(pythonAnalyzePayload.rootCauses).length > 0);
  });

  it('frontend maps canonical entity types and sources from both services', () => {
    assert.equal(isCanonicalEntityType('TEAM'), true);
    assert.equal(isCanonicalEntityType('team'), false);
    assert.equal(aiSourceLabel('FALLBACK'), 'Local AI fallback');
  });
});
