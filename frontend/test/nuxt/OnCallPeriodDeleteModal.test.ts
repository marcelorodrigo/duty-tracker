import { describe, it, expect, afterEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
import OnCallPeriodDeleteModal from '~/components/OnCallPeriodDeleteModal.vue'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

const mockPeriod: OnCallPeriodResponse = {
  id: 1,
  startDateTime: '2025-06-01T00:00:00',
  endDateTime: '2025-06-30T23:59:00',
  holidays: [],
  createdAt: '2025-05-01T00:00:00Z'
}

afterEach(() => {
  document.body.innerHTML = ''
})

describe('OnCallPeriodDeleteModal', () => {
  describe('body content', () => {
    it('renders the formatted date range in the confirmation message', async () => {
      await mountSuspended(OnCallPeriodDeleteModal, {
        props: { open: true, period: mockPeriod, confirming: false }
      })
      // formatDate('2025-06-01T00:00:00') → '01 Jun 2025'
      expect(document.body.textContent).toContain('01 Jun 2025')
      expect(document.body.textContent).toContain('30 Jun 2025')
    })

    it('does not render body/footer content when open=false', async () => {
      const wrapper = await mountSuspended(OnCallPeriodDeleteModal, {
        props: { open: false, period: mockPeriod, confirming: false }
      })
      // v-if="props.open" guards body and footer content inside UModal slots
      // Check the wrapper's own rendered HTML (not teleported body)
      expect(wrapper.text()).not.toContain('01 Jun 2025')
    })
  })

  describe('actions', () => {
    it('emits close when Cancel is clicked', async () => {
      const wrapper = await mountSuspended(OnCallPeriodDeleteModal, {
        props: { open: true, period: mockPeriod, confirming: false }
      })
      const cancelButton = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.trim() === 'Cancel'
      )
      cancelButton?.click()
      await flushPromises()
      expect(wrapper.emitted('close')).toEqual([[]])
    })

    it('emits confirm when Delete is clicked', async () => {
      const wrapper = await mountSuspended(OnCallPeriodDeleteModal, {
        props: { open: true, period: mockPeriod, confirming: false }
      })
      const deleteButton = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.trim() === 'Delete'
      )
      deleteButton?.click()
      await flushPromises()
      expect(wrapper.emitted('confirm')).toEqual([[]])
    })

    it('shows loading state on Delete button while confirmation is in-flight', async () => {
      const wrapper = await mountSuspended(OnCallPeriodDeleteModal, {
        props: { open: true, period: mockPeriod, confirming: false }
      })

      await wrapper.setProps({ confirming: true })
      await wrapper.vm.$nextTick()

      const deleteButtonAfter = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.includes('Delete')
      )
      expect(deleteButtonAfter?.hasAttribute('disabled')).toBe(true)
    })
  })
})
