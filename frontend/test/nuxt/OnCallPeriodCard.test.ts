import { describe, it, expect, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import OnCallPeriodCard from '~/components/OnCallPeriodCard.vue'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

const now = new Date()

const futurePeriod: OnCallPeriodResponse = {
  id: 1,
  startDateTime: new Date(now.getTime() + 60 * 60 * 1000).toISOString(),
  endDateTime: new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString(),
  holidays: [],
  createdAt: '2026-01-01T00:00:00Z'
}

const activePeriod: OnCallPeriodResponse = {
  id: 2,
  startDateTime: new Date(now.getTime() - 60 * 60 * 1000).toISOString(),
  endDateTime: new Date(now.getTime() + 60 * 60 * 1000).toISOString(),
  holidays: [],
  createdAt: '2026-01-01T00:00:00Z'
}

const pastPeriod: OnCallPeriodResponse = {
  id: 3,
  startDateTime: '2020-01-01T14:00:00',
  endDateTime: '2020-01-08T14:00:00',
  holidays: [
    { date: '2020-01-06', name: 'New Year Holiday' },
    { date: '2020-01-07', name: null }
  ],
  createdAt: '2020-01-01T00:00:00Z'
}

const periodWithoutHolidays: OnCallPeriodResponse = {
  ...pastPeriod,
  id: 4,
  holidays: []
}

describe('OnCallPeriodCard', () => {
  describe('status badge', () => {
    it('shows "Scheduled" badge for a future period', async () => {
      const wrapper = await mountSuspended(OnCallPeriodCard, {
        props: { period: futurePeriod, onEdit: vi.fn(), onDelete: vi.fn() }
      })
      expect(wrapper.text()).toContain('Scheduled')
    })

    it('shows "Active" badge for an ongoing period', async () => {
      const wrapper = await mountSuspended(OnCallPeriodCard, {
        props: { period: activePeriod, onEdit: vi.fn(), onDelete: vi.fn() }
      })
      expect(wrapper.text()).toContain('Active')
    })

    it('shows "Past" badge for an ended period', async () => {
      const wrapper = await mountSuspended(OnCallPeriodCard, {
        props: { period: pastPeriod, onEdit: vi.fn(), onDelete: vi.fn() }
      })
      expect(wrapper.text()).toContain('Past')
    })
  })

  describe('date range', () => {
    it('renders formatted start and end date/time', async () => {
      const wrapper = await mountSuspended(OnCallPeriodCard, {
        props: { period: pastPeriod, onEdit: vi.fn(), onDelete: vi.fn() }
      })
      // '2020-01-01T14:00:00' → '01 Jan 2020 14:00'
      expect(wrapper.text()).toContain('01 Jan 2020 14:00')
      expect(wrapper.text()).toContain('08 Jan 2020 14:00')
    })
  })

  describe('holidays', () => {
    it('renders holiday names and dates when holidays are present', async () => {
      const wrapper = await mountSuspended(OnCallPeriodCard, {
        props: { period: pastPeriod, onEdit: vi.fn(), onDelete: vi.fn() }
      })
      expect(wrapper.text()).toContain('Holidays')
      expect(wrapper.text()).toContain('New Year Holiday')
      // Null holiday name falls back to 'Holiday'
      expect(wrapper.text()).toContain('Holiday')
    })

    it('does not render holidays section when holidays array is empty', async () => {
      const wrapper = await mountSuspended(OnCallPeriodCard, {
        props: { period: periodWithoutHolidays, onEdit: vi.fn(), onDelete: vi.fn() }
      })
      expect(wrapper.text()).not.toContain('Holidays')
    })
  })

  describe('actions', () => {
    it('calls onEdit with the period when edit button is clicked', async () => {
      const onEdit = vi.fn()
      const wrapper = await mountSuspended(OnCallPeriodCard, {
        props: { period: pastPeriod, onEdit, onDelete: vi.fn() }
      })
      await wrapper.find('[aria-label="Edit period"]').trigger('click')
      expect(onEdit).toHaveBeenCalledOnce()
      expect(onEdit).toHaveBeenCalledWith(pastPeriod)
    })

    it('calls onDelete with the period when delete button is clicked', async () => {
      const onDelete = vi.fn()
      const wrapper = await mountSuspended(OnCallPeriodCard, {
        props: { period: pastPeriod, onEdit: vi.fn(), onDelete }
      })
      await wrapper.find('[aria-label="Delete period"]').trigger('click')
      expect(onDelete).toHaveBeenCalledOnce()
      expect(onDelete).toHaveBeenCalledWith(pastPeriod)
    })
  })
})
