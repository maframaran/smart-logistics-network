# Feature

Consultant Registration

## Goal

Allow consultants to create a searchable profile.

## Actors

- Consultant

## Preconditions

- User authenticated

## Workflow

1. User opens profile page
2. User enters information
3. User submits

## Business Rules

BR-001 Email must be verified

BR-002 At least one specialty required

BR-003 Profile visibility defaults to private

## Edge Cases

EC-001 Duplicate specialty

EC-002 Invalid website URL

## Acceptance Criteria

AC-001 Profile saved

AC-002 Search index updated

AC-003 Audit event created

## Telemetry

Track:
- registration_started
- registration_completed