import { describe, expect, it, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import IncidentDialog from '~/components/IncidentDialog.vue'
import type { IncidentResponse, CreateIncidentRequest } from '~/types/incident'

describe('IncidentDialog', () => {
  const mockIncident: IncidentResponse = {
    id: 1,
    onCallPeriodId: 10,
    name: 'Database failover',
    date: '2025-06-03',
    startTime: '02:30:00',
    endTime: '04:15:00',
    createdAt: '2025-06-03T10:00:00Z'
  }

  it('renders create mode title', async () => {
    const onClose = vi.fn()
    const onSubmit = vi.fn()
    const component = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onClose,
        onSubmit
      }
    })

    expect(component.text()).toContain('Log incident')
  })

  it('renders edit mode title with incident data', async () => {
    const onClose = vi.fn()
    const onSubmit = vi.fn()
    const component = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'edit' as const,
        incident: mockIncident,
        onCallPeriodId: 10,
        onClose,
        onSubmit
      }
    })

    expect(component.text()).toContain('Edit incident')
  })

  it('renders name input, date picker, and time fields', async () => {
    const onClose = vi.fn()
    const onSubmit = vi.fn()
    const component = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onClose,
        onSubmit
      }
    })

    expect(component.text()).toContain('Name')
    expect(component.text()).toContain('Date')
    expect(component.text()).toContain('Start time')
    expect(component.text()).toContain('End time')
  })

  it('shows validation error when name is empty on submit', async () => {
    const onClose = vi.fn()
    const onSubmit = vi.fn()
    const component = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onClose,
        onSubmit
      }
    })

    // Submit without filling anything
    const submitButton = component.findAll('button').find(b => b.text() === 'Log incident')
    await submitButton!.trigger('click')
    await component.vm.$nextTick()

    expect(component.text()).toContain('Name is required')
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('populates fields when editing an incident', async () => {
    const onClose = vi.fn()
    const onSubmit = vi.fn()
    const component = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'edit' as const,
        incident: mockIncident,
        onCallPeriodId: 10,
        onClose,
        onSubmit
      }
    })

    const nameInput = component.find('input[type="text"], input:not([type])')
    expect((nameInput.element as HTMLInputElement).value).toBe('Database failover')
  })

  it('calls onClose when cancel is clicked', async () => {
    const onClose = vi.fn()
    const onSubmit = vi.fn()
    const component = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onClose,
        onSubmit
      }
    })

    const cancelButton = component.findAll('button').find(b => b.text() === 'Cancel')
    await cancelButton!.trigger('click')

    expect(onClose).toHaveBeenCalledOnce()
  })

  it('submits create request with filled fields', async () => {
    const onClose = vi.fn()
    const onSubmit = vi.fn(async (_req: CreateIncidentRequest) => {})
    const component = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onClose,
        onSubmit
      }
    })

    // Fill name
    const nameInput = component.find('input[type="text"], input:not([type="time"])')
    await nameInput.setValue('Network outage')

    // Fill time inputs
    const timeInputs = component.findAll('input[type="time"]')
    await timeInputs[0]!.setValue('03:00')
    await timeInputs[1]!.setValue('05:30')

    // Submit
    const submitButton = component.findAll('button').find(b => b.text() === 'Log incident')
    await submitButton!.trigger('click')
    await component.vm.$nextTick()

    expect(onSubmit).toHaveBeenCalledOnce()
    const req = onSubmit.mock.calls[0]![0] as CreateIncidentRequest
    expect(req.onCallPeriodId).toBe(10)
    expect(req.name).toBe('Network outage')
    expect(req.startTime).toBe('03:00:00')
    expect(req.endTime).toBe('05:30:00')
  })
})
