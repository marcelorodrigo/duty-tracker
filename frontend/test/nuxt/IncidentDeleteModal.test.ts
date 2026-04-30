import { describe, expect, it, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import IncidentDeleteModal from '~/components/IncidentDeleteModal.vue'
import type { IncidentResponse } from '~/types/incident'

describe('IncidentDeleteModal', () => {
  const mockIncident: IncidentResponse = {
    id: 1,
    onCallPeriodId: 10,
    name: 'Database failover',
    date: '2025-06-03',
    startTime: '02:30:00',
    endTime: '04:15:00',
    createdAt: '2025-06-03T10:00:00Z'
  }

  it('renders incident name and time range in confirmation message', async () => {
    const onClose = vi.fn()
    const onConfirm = vi.fn()
    const component = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose, onConfirm }
    })

    expect(component.text()).toContain('Database failover')
    expect(component.text()).toContain('02:30')
    expect(component.text()).toContain('04:15')
  })

  it('renders delete and cancel buttons', async () => {
    const onClose = vi.fn()
    const onConfirm = vi.fn()
    const component = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose, onConfirm }
    })

    const buttons = component.findAll('button')
    const buttonTexts = buttons.map(b => b.text())
    expect(buttonTexts).toContain('Cancel')
    expect(buttonTexts).toContain('Delete')
  })

  it('calls onClose when cancel is clicked', async () => {
    const onClose = vi.fn()
    const onConfirm = vi.fn()
    const component = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose, onConfirm }
    })

    const cancelButton = component.findAll('button').find(b => b.text() === 'Cancel')
    await cancelButton!.trigger('click')

    expect(onClose).toHaveBeenCalledOnce()
  })

  it('calls onConfirm when delete is clicked', async () => {
    const onClose = vi.fn()
    const onConfirm = vi.fn(async () => {})
    const component = await mountSuspended(IncidentDeleteModal, {
      props: { open: true, incident: mockIncident, onClose, onConfirm }
    })

    const deleteButton = component.findAll('button').find(b => b.text() === 'Delete')
    await deleteButton!.trigger('click')

    expect(onConfirm).toHaveBeenCalledOnce()
  })
})
