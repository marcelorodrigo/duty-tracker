import { describe, expect, it, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import IncidentDeleteModal from '~/components/IncidentDeleteModal.vue'
import type { IncidentResponse } from '~/types/incident'

describe('IncidentDeleteModal', () => {
  const mockIncident: IncidentResponse = {
    id: 1,
    onCallPeriodId: 10,
    name: 'Database failover',
    startDateTime: '2025-06-03T02:30:00',
    endDateTime: '2025-06-03T04:15:00',
    createdAt: '2025-06-03T10:00:00Z'
  }

  it('renders incident name in confirmation message', async () => {
    await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose: vi.fn(), onConfirm: vi.fn() }
    })

    expect(document.body.textContent).toContain('Database failover')
  })

  it('renders delete and cancel buttons', async () => {
    await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose: vi.fn(), onConfirm: vi.fn() }
    })

    const buttons = Array.from(document.body.querySelectorAll('button')).map(b => b.textContent?.trim())
    expect(buttons).toContain('Cancel')
    expect(buttons).toContain('Delete')
  })

  it.skip('calls onClose when cancel is clicked (requires teleport-aware test utilities)', async () => {
    const onClose = vi.fn()
    const wrapper = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose, onConfirm: vi.fn() }
    })

    const cancelButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Cancel'
    )
    cancelButton?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await wrapper.vm.$nextTick()

    expect(onClose).toHaveBeenCalledOnce()
  })

  it.skip('calls onConfirm when delete is clicked (requires teleport-aware test utilities)', async () => {
    const onConfirm = vi.fn(async () => {})
    const wrapper = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose: vi.fn(), onConfirm }
    })

    const deleteButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Delete'
    )
    deleteButton?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await wrapper.vm.$nextTick()

    expect(onConfirm).toHaveBeenCalledOnce()
  })
})
