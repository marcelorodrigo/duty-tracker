import { describe, expect, it, vi, afterEach } from 'vitest'
import { nextTick } from 'vue'
import type { DateValue } from '@internationalized/date'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
import IncidentDialog from '~/components/IncidentDialog.vue'
import type { IncidentResponse } from '~/types/incident'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

interface IncidentDialogViewModel {
  name: string
  startDateTime: DateValue | undefined
}

function getDialogViewModel(wrapper: { vm: unknown }): IncidentDialogViewModel {
  return wrapper.vm as IncidentDialogViewModel
}

afterEach(() => {
  document.body.innerHTML = ''
})

describe('IncidentDialog', () => {
  const mockPeriod: OnCallPeriodResponse = {
    id: 10,
    startDateTime: '2025-06-01T00:00:00',
    endDateTime: '2025-06-30T23:59:00',
    holidays: [],
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
    submitButton?.click()
    await wrapper.vm.$nextTick()

    expect(document.body.textContent).toContain('Name is required')
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('populates name reactive ref when editing an incident', async () => {
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
    await nextTick()

    // The watch sets name.value — verify via the vm's exposed reactive state
    expect(getDialogViewModel(wrapper).name).toBe('Database failover')
  })

  it('calls onClose when cancel is clicked', async () => {
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
    cancelButton?.click()
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

    // Set name directly on the vm reactive state (DOM .value doesn't update Vue refs)
    const viewModel = getDialogViewModel(wrapper)
    viewModel.name = 'Test incident'
    // Clear startDateTime to ensure it's undefined
    viewModel.startDateTime = undefined

    const submitButton = Array.from(document.body.querySelectorAll('button')).find(
      b => b.textContent?.trim() === 'Log incident'
    )
    submitButton?.click()
    await wrapper.vm.$nextTick()

    expect(document.body.textContent).toContain('required')
    expect(onSubmit).not.toHaveBeenCalled()
  })

  describe('period boundary validation', () => {
    it('shows error when startDateTime is before the period start', async () => {
      const onSubmit = vi.fn()
      const wrapper = await mountSuspended(IncidentDialog, {
        props: {
          open: false,
          mode: 'edit' as const,
          incident: {
            ...mockIncident,
            // before period start (2025-06-01)
            startDateTime: '2025-05-31T10:00:00',
            endDateTime: '2025-05-31T11:00:00'
          },
          onCallPeriodId: 10,
          onCallPeriod: mockPeriod,
          onClose: vi.fn(),
          onSubmit
        }
      })

      await wrapper.setProps({ open: true })
      await nextTick()

      // Verify reactive state was set by the watch
      const viewModel = getDialogViewModel(wrapper)
      expect(viewModel.startDateTime).toBeTruthy()
      expect(viewModel.name).toBe('Database failover')

      const submitButton = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.trim() === 'Save changes'
      )
      submitButton?.click()
      await wrapper.vm.$nextTick()

      expect(document.body.textContent).toContain('must be within the on-call period window')
      expect(onSubmit).not.toHaveBeenCalled()
    })

    it('shows error when startDateTime is after the period end', async () => {
      const onSubmit = vi.fn()
      const wrapper = await mountSuspended(IncidentDialog, {
        props: {
          open: false,
          mode: 'edit' as const,
          incident: {
            ...mockIncident,
            // after period end (2025-06-30T23:59:00)
            startDateTime: '2025-07-01T10:00:00',
            endDateTime: '2025-07-01T11:00:00'
          },
          onCallPeriodId: 10,
          onCallPeriod: mockPeriod,
          onClose: vi.fn(),
          onSubmit
        }
      })

      await wrapper.setProps({ open: true })
      await nextTick()

      const submitButton = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.trim() === 'Save changes'
      )
      submitButton?.click()
      await wrapper.vm.$nextTick()

      expect(document.body.textContent).toContain('must be within the on-call period window')
      expect(onSubmit).not.toHaveBeenCalled()
    })
  })

  describe('end date warning modal', () => {
    it('shows warning modal when endDateTime is after period end', async () => {
      const onSubmit = vi.fn()
      const wrapper = await mountSuspended(IncidentDialog, {
        props: {
          open: false,
          mode: 'edit' as const,
          incident: {
            ...mockIncident,
            startDateTime: '2025-06-15T10:00:00',
            // end is after period end
            endDateTime: '2025-07-05T11:00:00'
          },
          onCallPeriodId: 10,
          onCallPeriod: mockPeriod,
          onClose: vi.fn(),
          onSubmit
        }
      })

      await wrapper.setProps({ open: true })
      await nextTick()

      const submitButton = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.trim() === 'Save changes'
      )
      submitButton?.click()
      await wrapper.vm.$nextTick()

      expect(document.body.textContent).toContain('End time outside period')
      expect(document.body.textContent).toContain('Continue anyway')

      // Find "Continue anyway" button to confirm warning modal is open, then click the Cancel button
      // that immediately precedes it (both are in the warning modal's footer)
      const allButtons = Array.from(document.body.querySelectorAll('button'))
      const continueIndex = allButtons.findIndex(b => b.textContent?.trim() === 'Continue anyway')
      // The Cancel button in the warning modal is right before "Continue anyway"
      allButtons[continueIndex - 1]?.click()
      await flushPromises()

      expect(document.body.textContent).not.toContain('End time outside period')
      expect(onSubmit).not.toHaveBeenCalled()
    })

    it('submits when Continue anyway is clicked in warning modal', async () => {
      const onSubmit = vi.fn().mockResolvedValue(undefined)
      const wrapper = await mountSuspended(IncidentDialog, {
        props: {
          open: false,
          mode: 'edit' as const,
          incident: {
            ...mockIncident,
            startDateTime: '2025-06-15T10:00:00',
            endDateTime: '2025-07-05T11:00:00'
          },
          onCallPeriodId: 10,
          onCallPeriod: mockPeriod,
          onClose: vi.fn(),
          onSubmit
        }
      })

      await wrapper.setProps({ open: true })
      await nextTick()

      // Click submit to show warning modal
      const submitButton = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.trim() === 'Save changes'
      )
      submitButton?.click()
      await nextTick()

      expect(document.body.textContent).toContain('Continue anyway')

      // Click "Continue anyway"
      const continueButton = Array.from(document.body.querySelectorAll('button')).find(
        b => b.textContent?.trim() === 'Continue anyway'
      )
      continueButton?.click()
      await flushPromises()

      expect(onSubmit).toHaveBeenCalledOnce()
      expect(onSubmit).toHaveBeenCalledWith({
        name: 'Database failover',
        startDateTime: '2025-06-15T10:00:00',
        endDateTime: '2025-07-05T11:00:00'
      })
    })
  })
})
