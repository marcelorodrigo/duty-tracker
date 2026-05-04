import { describe, expect, it, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import IncidentDialog from '~/components/IncidentDialog.vue'
import type { IncidentResponse, CreateIncidentRequest } from '~/types/incident'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

describe('IncidentDialog', () => {
  const mockPeriod: OnCallPeriodResponse = {
    id: 10,
    startDateTime: '2025-06-01T00:00:00',
    endDateTime: '2025-06-30T23:59:00',
    holidayOverrides: [],
    createdAt: '2025-05-01T00:00:00Z'
  }

  const mockIncident: IncidentResponse = {
    id: 1,
    onCallPeriodId: 10,
    name: 'Database failover',
    startDateTime: '2025-06-03T02:30:00',
    endDateTime: '2025-06-03T04:15:00',
    createdAt: '2025-06-03T10:00:00Z'
  }

  it('renders create mode title', async () => {
    await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onCallPeriod: mockPeriod,
        onClose: vi.fn(),
        onSubmit: vi.fn()
      }
    })

    expect(document.body.textContent).toContain('Log incident')
  })

  it('renders edit mode title with incident data', async () => {
    await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'edit' as const,
        incident: mockIncident,
        onCallPeriodId: 10,
        onCallPeriod: mockPeriod,
        onClose: vi.fn(),
        onSubmit: vi.fn()
      }
    })

    expect(document.body.textContent).toContain('Edit incident')
  })

  it('renders name input, start date/time and end date/time fields', async () => {
    await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onCallPeriod: mockPeriod,
        onClose: vi.fn(),
        onSubmit: vi.fn()
      }
    })

    expect(document.body.textContent).toContain('Name')
    expect(document.body.textContent).toContain('Start date/time')
    expect(document.body.textContent).toContain('End date/time')
  })

  it('shows validation error when name is empty on submit', async () => {
    const onSubmit = vi.fn()
    const wrapper = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onCallPeriod: mockPeriod,
        onClose: vi.fn(),
        onSubmit
      }
    })

    const submitButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Log incident'
    )
    submitButton?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await wrapper.vm.$nextTick()

    expect(document.body.textContent).toContain('Name is required')
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it.skip('populates name field when editing an incident (UInput renders in teleport, hard to inspect value)', async () => {
    const wrapper = await mountSuspended(IncidentDialog, {
      props: {
        open: false,
        mode: 'edit' as const,
        incident: mockIncident,
        onCallPeriodId: 10,
        onCallPeriod: mockPeriod,
        onClose: vi.fn(),
        onSubmit: vi.fn()
      }
    })

    // Open the dialog to trigger the watch
    await wrapper.setProps({ open: true })
    await wrapper.vm.$nextTick()

    const nameInput = document.body.querySelector('input[type="text"], input:not([type])')
    expect((nameInput as HTMLInputElement)?.value).toBe('Database failover')
  })

  it.skip('calls onClose when cancel is clicked (requires teleport-aware test utilities)', async () => {
    const onClose = vi.fn()
    const wrapper = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onCallPeriod: mockPeriod,
        onClose,
        onSubmit: vi.fn()
      }
    })

    const cancelButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Cancel'
    )
    cancelButton?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await wrapper.vm.$nextTick()

    expect(onClose).toHaveBeenCalledOnce()
  })

  it('shows validation error when startDateTime is missing on submit', async () => {
    const onSubmit = vi.fn()
    const wrapper = await mountSuspended(IncidentDialog, {
      props: {
        open: true,
        mode: 'create' as const,
        incident: null,
        onCallPeriodId: 10,
        onCallPeriod: mockPeriod,
        onClose: vi.fn(),
        onSubmit
      }
    })

    // Fill name but leave dates empty
    const nameInput = document.body.querySelector('input[type="text"], input:not([type])')
    if (nameInput) {
      ;(nameInput as HTMLInputElement).value = 'Test incident'
      nameInput.dispatchEvent(new Event('input'))
    }

    const submitButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Log incident'
    )
    submitButton?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await wrapper.vm.$nextTick()

    expect(document.body.textContent).toContain('required')
    expect(onSubmit).not.toHaveBeenCalled()
  })
})
