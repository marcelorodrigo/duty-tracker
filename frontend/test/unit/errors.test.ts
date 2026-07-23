import { describe, expect, it } from 'vitest'
import { ApiProblem } from '~/utils/api'
import { getApiErrorMessage } from '~/utils/errors'

const UNTRUSTED_DETAIL = 'SQLSTATE 23505: secret_table_internal_idx'

describe('getApiErrorMessage', () => {
  it.each([
    {
      status: 400,
      expected: 'Some information is invalid. Please review it and try again.'
    },
    {
      status: 404,
      expected: 'The requested item could not be found.'
    },
    {
      status: 409,
      expected: 'This request conflicts with existing data. Refresh the page and try again.'
    },
    {
      status: 500,
      expected: 'The server could not complete the request. Please try again later.'
    }
  ])('maps status $status without exposing backend detail', ({ status, expected }) => {
    const error = new ApiProblem({ status, detail: UNTRUSTED_DETAIL })

    const message = getApiErrorMessage(error)

    expect(message).toBe(expected)
    expect(message).not.toContain(UNTRUSTED_DETAIL)
  })

  it('prefers a controlled message for a known stable problem type', () => {
    const error = new ApiProblem({
      type: 'https://duty-tracker.example/errors/incident-overlap',
      status: 409,
      detail: UNTRUSTED_DETAIL
    })

    expect(getApiErrorMessage(error)).toBe('This incident overlaps another incident.')
  })

  it('falls back to the status for an unknown problem type', () => {
    const error = new ApiProblem({
      type: 'https://duty-tracker.example/errors/future-problem',
      status: 404,
      detail: UNTRUSTED_DETAIL
    })

    expect(getApiErrorMessage(error)).toBe('The requested item could not be found.')
  })

  it('maps a transport failure without exposing its message', () => {
    const message = getApiErrorMessage(new Error(UNTRUSTED_DETAIL))

    expect(message).toBe('We could not reach the server. Check your connection and try again.')
    expect(message).not.toContain(UNTRUSTED_DETAIL)
  })

  it('only reads stable status and type fields from an unnormalized response', () => {
    const error = {
      data: {
        status: 400,
        detail: UNTRUSTED_DETAIL,
        message: UNTRUSTED_DETAIL
      }
    }

    expect(getApiErrorMessage(error)).toBe('Some information is invalid. Please review it and try again.')
  })

  it('uses a controlled default for other client errors', () => {
    const error = new ApiProblem({ status: 403, detail: UNTRUSTED_DETAIL })

    expect(getApiErrorMessage(error)).toBe('We could not complete the request. Please try again.')
  })
})
