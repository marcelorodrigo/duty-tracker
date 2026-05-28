import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { defineComponent } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { useProfile } from '~/composables/useProfile'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'

const mockFetch = vi.fn()

const mockProfile: EngineerProfileResponse = {
  id: 1,
  workingDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
  workStartTime: '08:00:00',
  workEndTime: '16:30:00',
  hourlyRate: 50.0,
  standbyWeekdaySaturdayPercentage: 0.067,
  standbyWeekdaySundayHolidayPercentage: 0.084
}

beforeEach(() => {
  vi.stubGlobal('$fetch', mockFetch)
  // Default: return mockProfile so useFetch picks it up on composable init
  mockFetch.mockResolvedValue(mockProfile)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

async function withComposable(): Promise<ReturnType<typeof useProfile>> {
  let composable!: ReturnType<typeof useProfile>

  await mountSuspended(defineComponent({
    setup() {
      composable = useProfile()
      return () => null
    }
  }))

  await flushPromises()
  return composable
}

describe('useProfile', () => {
  describe('initial state', () => {
    it('loads profile via useFetch on mount', async () => {
      const { profile, pending, error } = await withComposable()

      expect(profile.value).toEqual(mockProfile)
      expect(pending.value).toBe(false)
      // useFetch error ref starts as undefined (not null) when no error occurred
      expect(error.value).toBeUndefined()
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
      const { save } = await withComposable()
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
      const composable = await withComposable()
      mockFetch.mockResolvedValueOnce({ ...mockProfile, ...updateRequest })

      // Kick off save without awaiting to inspect mid-flight state
      const savePromise = composable.save(updateRequest)
      // At this point optimistic update has been applied synchronously
      const optimisticValue = composable.profile.value ? { ...composable.profile.value } : null
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
      const { save, profile } = await withComposable()
      mockFetch.mockResolvedValueOnce(serverResponse)

      await save(updateRequest)

      expect(profile.value).toEqual(serverResponse)
    })

    it('reverts to the original profile on failure', async () => {
      const { save, profile } = await withComposable()
      // Ensure profile is populated (guard against useFetch caching between tests)
      profile.value = { ...mockProfile }
      const originalProfile = { ...mockProfile }
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      await save(updateRequest)

      expect(profile.value).toEqual(originalProfile)
    })

    it('does not change profile when save fails and profile was null', async () => {
      const composable = await withComposable()
      composable.profile.value = null
      mockFetch.mockRejectedValueOnce(new Error('Server error'))

      // Should not throw
      await composable.save(updateRequest)
      expect(composable.profile.value).toBeNull()
    })
  })
})
