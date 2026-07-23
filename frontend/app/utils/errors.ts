import { ApiProblem } from '~/utils/api'

const PROBLEM_TYPE_MESSAGES: Readonly<Record<string, string>> = Object.freeze({
  'profile-already-exists': 'A profile already exists.',
  'invalid-engineer-profile': 'The profile contains invalid information.',
  'profile-not-found': 'Your profile could not be found.',
  'invalid-oncall-period': 'The on-call period could not be found.',
  'oncall-period-overlap': 'This on-call period overlaps an existing period.',
  'invalid-incident': 'The incident contains invalid information.',
  'incident-not-found': 'The incident could not be found.',
  'incident-overlap': 'This incident overlaps another incident.',
  'holiday-already-registered': 'This holiday is already registered.',
  'incident-during-working-hours': 'Incidents cannot be logged during working hours.',
  'duplicate-compensation-rate': 'This compensation rate already exists.',
  'compensation-rate-not-found': 'The compensation rate could not be found.',
  'invalid-compensation-rate': 'The compensation rate contains invalid information.',
  'invalid-holiday-suggestion-range': 'Choose a valid date range for holiday suggestions.',
  'invalid-hourly-rate': 'Enter a valid hourly rate.',
  'invalid-standby-percentage': 'Enter a valid standby percentage.'
})

const STATUS_MESSAGES: Readonly<Record<number, string>> = Object.freeze({
  400: 'Some information is invalid. Please review it and try again.',
  404: 'The requested item could not be found.',
  409: 'This request conflicts with existing data. Refresh the page and try again.'
})

const SERVER_ERROR_MESSAGE = 'The server could not complete the request. Please try again later.'
const CONNECTION_ERROR_MESSAGE = 'We could not reach the server. Check your connection and try again.'
const DEFAULT_ERROR_MESSAGE = 'We could not complete the request. Please try again.'

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function integerValue(value: unknown): number | undefined {
  return typeof value === 'number' && Number.isInteger(value) ? value : undefined
}

function stringValue(value: unknown): string | undefined {
  return typeof value === 'string' ? value : undefined
}

function problemStatus(error: unknown): number | undefined {
  if (error instanceof ApiProblem) return error.status
  if (!isRecord(error)) return undefined

  const data = isRecord(error.data) ? error.data : undefined
  const response = isRecord(error.response) ? error.response : undefined

  return integerValue(data?.status)
    ?? integerValue(error.statusCode)
    ?? integerValue(error.status)
    ?? integerValue(response?.status)
}

function problemType(error: unknown): string | undefined {
  if (error instanceof ApiProblem) return error.type
  if (!isRecord(error)) return undefined

  const data = isRecord(error.data) ? error.data : undefined
  return stringValue(data?.type) ?? stringValue(error.type)
}

function messageForProblemType(type: string | undefined): string | undefined {
  if (!type) return undefined

  const match = type.match(/(?:^|\/)errors\/([^/?#]+)$/)
  return match?.[1] ? PROBLEM_TYPE_MESSAGES[match[1]] : undefined
}

/**
 * Maps API failures to controlled text that is safe to render.
 *
 * Backend titles, details, messages, and other response fields are deliberately
 * ignored. They remain available on ApiProblem for diagnostic use only.
 */
export function getApiErrorMessage(error: unknown): string {
  const typeMessage = messageForProblemType(problemType(error))
  if (typeMessage) return typeMessage

  const status = problemStatus(error)
  if (status !== undefined) {
    const statusMessage = STATUS_MESSAGES[status]
    if (statusMessage) return statusMessage
    if (status >= 500) return SERVER_ERROR_MESSAGE
    return DEFAULT_ERROR_MESSAGE
  }

  return CONNECTION_ERROR_MESSAGE
}
