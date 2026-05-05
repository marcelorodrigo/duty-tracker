import { describe, expect, it } from 'vitest'
import { getPeriodStatus } from '~/utils/dates'

const now = new Date()

describe('OnCall Detail Page - Period Status Logic', () => {
  describe('getPeriodStatus determines button visibility', () => {
    it('returns "active" for currently ongoing periods - buttons should be visible', () => {
      const pastStart = new Date(now.getTime() - 60 * 60 * 1000) // 1 hour ago
      const futureEnd = new Date(now.getTime() + 60 * 60 * 1000) // 1 hour from now

      const status = getPeriodStatus(
        pastStart.toISOString(),
        futureEnd.toISOString()
      )

      // When status is 'active', buttons should be visible (v-if="status !== 'scheduled'")
      expect(status).toBe('active')
      expect(status !== 'scheduled').toBe(true)
    })

    it('returns "scheduled" for future periods - buttons should be hidden', () => {
      const futureStart = new Date(now.getTime() + 60 * 60 * 1000) // 1 hour from now
      const futureEnd = new Date(now.getTime() + 2 * 60 * 60 * 1000) // 2 hours from now

      const status = getPeriodStatus(
        futureStart.toISOString(),
        futureEnd.toISOString()
      )

      // When status is 'scheduled', buttons should be hidden (v-if="status !== 'scheduled'")
      expect(status).toBe('scheduled')
      expect(status !== 'scheduled').toBe(false)
    })

    it('returns "past" for ended periods - buttons should be visible', () => {
      const status = getPeriodStatus(
        '2020-01-01T14:00:00',
        '2020-01-08T14:00:00'
      )

      // When status is 'past', buttons should be visible (v-if="status !== 'scheduled'")
      expect(status).toBe('past')
      expect(status !== 'scheduled').toBe(true)
    })
  })

  describe('Status display text', () => {
    it('displays "Scheduled" when period is in the future', () => {
      const futureStart = new Date(now.getTime() + 60 * 60 * 1000)
      const futureEnd = new Date(now.getTime() + 2 * 60 * 60 * 1000)

      const status = getPeriodStatus(
        futureStart.toISOString(),
        futureEnd.toISOString()
      )

      const statusText = status === 'scheduled' ? 'Scheduled' : status === 'active' ? 'Active' : 'Past'
      expect(statusText).toBe('Scheduled')
    })

    it('displays "Active" when period is currently ongoing', () => {
      const pastStart = new Date(now.getTime() - 60 * 60 * 1000)
      const futureEnd = new Date(now.getTime() + 60 * 60 * 1000)

      const status = getPeriodStatus(
        pastStart.toISOString(),
        futureEnd.toISOString()
      )

      const statusText = status === 'scheduled' ? 'Scheduled' : status === 'active' ? 'Active' : 'Past'
      expect(statusText).toBe('Active')
    })

    it('displays "Past" when period has ended', () => {
      const status = getPeriodStatus(
        '2020-01-01T14:00:00',
        '2020-01-08T14:00:00'
      )

      const statusText = status === 'scheduled' ? 'Scheduled' : status === 'active' ? 'Active' : 'Past'
      expect(statusText).toBe('Past')
    })
  })
})
