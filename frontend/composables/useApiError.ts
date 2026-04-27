import type { ProblemDetail } from './useApi'

const ERROR_MESSAGES: Record<string, string> = {
  'https://dutytracker/errors/profile-already-exists':
    'A profile already exists. You can only have one engineer profile.',
  'https://dutytracker/errors/profile-locked':
    'Your profile is locked. Profile cannot be changed after registrations exist.',
  'https://dutytracker/errors/onboarding-incomplete':
    'Please complete the onboarding wizard before using this feature.',
  'https://dutytracker/errors/invalid-oncall-period':
    'The on-call period dates are invalid. End time must be after start time.',
  'https://dutytracker/errors/invalid-incident':
    'The incident details are invalid. Check the date and time.',
  'https://dutytracker/errors/holiday-already-registered':
    'This date is already registered as a holiday for this period.',
  'https://dutytracker/errors/incident-during-working-hours':
    'This incident falls entirely within normal working hours. No overtime applies.',
  'https://dutytracker/errors/overtime-day-off':
    'This day is flagged as a day off. Time-for-time applies instead of overtime.',
}

export const getErrorMessage = (pd: ProblemDetail): string => {
  if (pd.type && ERROR_MESSAGES[pd.type]) {
    return ERROR_MESSAGES[pd.type]
  }
  return pd.detail ?? pd.title ?? 'An unexpected error occurred.'
}
