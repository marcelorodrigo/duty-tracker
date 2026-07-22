import { describe, it, expect } from 'vitest'
import { useProfile } from '~/composables/useProfile'
import { withComposable } from '../utils/test-composable'
import { setupFetchMock } from '../utils/mock-fetch'
import { buildProfile } from '../utils/factories'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'

const mockProfile = buildProfile()

const mockFetch = setupFetchMock(mockProfile)

describe('useProfile', () => {
  describe('refresh()', () => {
    it('loads profile via the API client', async () => {
      const { data, pending, error, refresh } = await withComposable(() => useProfile())

      await refresh()

      expect(data.value).toEqual(mockProfile)
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

    it('applies optimistic update before the request completes', async () => {
      const composable = await withComposable(() => useProfile())
      composable.data.value = { ...mockProfile }
      mockFetch.mockResolvedValueOnce({ ...mockProfile, ...updateRequest })

      // Kick off save without awaiting to inspect mid-flight state
      const savePromise = composable.save(updateRequest)
      // At this point optimistic update has been applied synchronously
      const optimisticValue = composable.data.value ? { ...composable.data.value } : null
      await savePromise

      expect(optimisticValue?.workStartTime).toBe(updateRequest.workStartTime)
      expect(optimisticValue?.workEndTime).toBe(updateRequest.workEndTime)
    })

    it('sets profile to the server response on success', async () => {
      const serverResponse: EngineerProfileResponse = {
        ...mockProfile,
        workStartTime: '09:00:00',
        workEndTime: '17:30:00'
      }
      const { save, data } = await withComposable(() => useProfile())
      mockFetch.mockResolvedValueOnce(serverResponse)

      await save(updateRequest)

      expect(data.value).toEqual(serverResponse)
    })

    it('reverts to the original profile on failure', async () => {
      const { save, data } = await withComposable(() => useProfile())
      data.value = { ...mockProfile }
      const originalProfile = { ...mockProfile }
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await save(updateRequest)

      expect(data.value).toEqual(originalProfile)
    })

    it('does not change profile when save fails and data was null', async () => {
      const composable = await withComposable(() => useProfile())
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      // Should not throw
      await composable.save(updateRequest)
      expect(composable.data.value).toBeNull()
    })
  })
})
