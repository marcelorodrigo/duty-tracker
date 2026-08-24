import { describe, it, expect, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { useProfile } from '~/composables/useProfile'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildProfile } from '../utils/factories'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'

const mockProfile = buildProfile()

const mockFetch = setupFetchMock(mockProfile)

describe('useProfile', () => {
  beforeEach(() => {
    mockFetch.mockReset()
    mockFetch.mockResolvedValue(mockProfile)
  })

  describe('initial state', () => {
    it('loads profile via the query on mount', async () => {
      const { profile, pending, error } = await withComposable(() => useProfile())

      expect(profile.value).toEqual(mockProfile)
      expect(pending.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('save()', () => {
    const updateRequest: UpdateProfileRequest = {
      workingDays: ['MONDAY', 'TUESDAY', 'THURSDAY', 'FRIDAY'],
      workStartTime: '09:00:00',
      workEndTime: '17:30:00',
      hourlyRate: 55.0,
      standbyWeekdaySaturdayPercentage: 0.067,
      standbyWeekdaySundayHolidayPercentage: 0.084
    }

    it('calls PUT to /api/v1/profile with the request body', async () => {
      const updatedProfile: EngineerProfileResponse = { ...mockProfile, ...updateRequest }
      const { save } = await withComposable(() => useProfile())
      mockFetch.mockResolvedValueOnce(updatedProfile)

      await save(updateRequest)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/v1/profile',
        expect.objectContaining({
          method: 'PUT',
          body: updateRequest
        })
      )
    })

    it('applies an optimistic update before the request completes', async () => {
      let resolvePut!: (value: unknown) => void
      const deferred = new Promise<unknown>((resolve) => {
        resolvePut = resolve
      })
      const composable = await withComposable(() => useProfile())
      mockFetch.mockReturnValueOnce(deferred)

      const savePromise = composable.save(updateRequest)
      await flushPromises()

      expect(composable.profile.value?.workStartTime).toBe(updateRequest.workStartTime)
      expect(composable.profile.value?.workEndTime).toBe(updateRequest.workEndTime)

      resolvePut({ ...mockProfile, ...updateRequest })
      await savePromise
      await flushPromises()
    })

    it('sets profile to the server response on success', async () => {
      const serverResponse: EngineerProfileResponse = {
        ...mockProfile,
        workStartTime: '09:00:00',
        workEndTime: '17:30:00'
      }
      const { save, profile } = await withComposable(() => useProfile())
      mockFetch.mockResolvedValueOnce(serverResponse) // PUT
      mockFetch.mockResolvedValueOnce(serverResponse) // refetch after invalidation

      await save(updateRequest)
      await flushPromises()

      expect(profile.value).toEqual(serverResponse)
    })

    it('reverts to the original profile on failure', async () => {
      const { save, profile } = await withComposable(() => useProfile())
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await save(updateRequest)

      expect(profile.value).toEqual(mockProfile)
    })

    it('does not change profile when save fails and profile was undefined', async () => {
      mockFetch.mockReturnValue(new Promise<unknown>(() => {}))
      const composable = await withComposable(() => useProfile())
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await composable.save(updateRequest)

      expect(composable.profile.value).toBeUndefined()
    })
  })
})
