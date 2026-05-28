import { describe, expect, it, vi, afterEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
import IncidentDeleteModal from '~/components/IncidentDeleteModal.vue'
import type { IncidentResponse } from '~/types/incident'

afterEach(() => {
  document.body.innerHTML = ''
})

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

  it('calls onClose when cancel is clicked', async () => {
    const onClose = vi.fn()
    const wrapper = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose, onConfirm: vi.fn() }
    })

    const cancelButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Cancel'
    )
    cancelButton?.click()
    await wrapper.vm.$nextTick()

    expect(onClose).toHaveBeenCalledOnce()
  })

  it('calls onConfirm when delete is clicked', async () => {
    const onConfirm = vi.fn(async () => {})
    const wrapper = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose: vi.fn(), onConfirm }
    })

    const deleteButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Delete'
    )
    deleteButton?.click()
    await wrapper.vm.$nextTick()
    await flushPromises()

    expect(onConfirm).toHaveBeenCalledOnce()
  })

  it('shows loading state on Delete button while onConfirm is in-flight', async () => {
    let resolveConfirm!: () => void
    const onConfirm = vi.fn(
      () => new Promise<void>(resolve => { resolveConfirm = resolve })
    )
    const wrapper = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose: vi.fn(), onConfirm }
    })

    const deleteButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Delete'
    )
    deleteButton?.click()
    await wrapper.vm.$nextTick()

    // deleting=true → Delete button should be disabled
    const deleteButtonAfter = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.includes('Delete')
    )
    expect(deleteButtonAfter?.hasAttribute('disabled')).toBe(true)

    resolveConfirm()
    await flushPromises()
  })

  it('displays formatted startDateTime–endDateTime for the incident', async () => {
    await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose: vi.fn(), onConfirm: vi.fn() }
    })

    // formatDateTime('2025-06-03T02:30:00') → '03 Jun 2025 02:30'
    expect(document.body.textContent).toContain('03 Jun 2025 02:30')
    expect(document.body.textContent).toContain('03 Jun 2025 04:15')
  })

  it('does not render incident details when incident prop is null', async () => {
    await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: null, onClose: vi.fn(), onConfirm: vi.fn() }
    })

    expect(document.body.textContent).not.toContain('Database failover')
  })
})
