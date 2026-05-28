import { describe, it, expect } from 'vitest'
import { extractErrorDetail } from '~/utils/errors'

describe('extractErrorDetail', () => {
  it('returns data.detail when present', () => {
    const err = { data: { detail: 'Validation failed', message: 'Bad request' } }
    expect(extractErrorDetail(err)).toBe('Validation failed')
  })

  it('returns data.message when detail is absent', () => {
    const err = { data: { message: 'Something went wrong' } }
    expect(extractErrorDetail(err)).toBe('Something went wrong')
  })

  it('returns the default fallback when data is an empty object', () => {
    const err = { data: {} }
    expect(extractErrorDetail(err)).toBe('Please try again.')
  })

  it('returns the default fallback for non-object errors', () => {
    expect(extractErrorDetail(null)).toBe('Please try again.')
    expect(extractErrorDetail('some string error')).toBe('Please try again.')
    expect(extractErrorDetail(42)).toBe('Please try again.')
  })

  it('returns the default fallback when there is no data property', () => {
    const err = { status: 500 }
    expect(extractErrorDetail(err)).toBe('Please try again.')
  })

  it('returns a custom fallback string when provided', () => {
    const err = { data: {} }
    expect(extractErrorDetail(err, 'Custom error message')).toBe('Custom error message')
  })
})
