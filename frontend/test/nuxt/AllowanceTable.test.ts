import { describe, expect, it, vi } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import SettingsAllowanceTable from '~/components/settings/AllowanceTable.vue'
import type { PivotRow } from '~/types/compensation'

describe('SettingsAllowanceTable', () => {
  const rows: PivotRow[] = [
    {
      slot: '00:00–01:00',
      timeFrom: '00:00:00',
      weekday: { id: 1, percentage: 50, label: 'Weekday 00:00' },
      saturday: { id: 2, percentage: 50, label: 'Saturday 00:00' },
      sundayHoliday: { id: 3, percentage: 100, label: 'Sunday 00:00' }
    },
    {
      slot: '01:00–02:00',
      timeFrom: '01:00:00',
      weekday: { id: 4, percentage: 50, label: 'Weekday 01:00' },
      saturday: { id: 5, percentage: 50, label: 'Saturday 01:00' },
      sundayHoliday: { id: 6, percentage: 100, label: 'Sunday 01:00' }
    }
  ]

  it('renders the allowance table headers and row values', async () => {
    const onSave = vi.fn().mockResolvedValue(undefined)
    const component = await mountSuspended(SettingsAllowanceTable, {
      props: { rows, onSave }
    })

    expect(component.text()).toContain('Time Slot')
    expect(component.text()).toContain('Monday to Friday')
    expect(component.text()).toContain('Saturday')
    expect(component.text()).toContain('Sunday / Holiday')
    expect(component.text()).toContain('00:00–01:00')
    expect(component.text()).toContain('01:00–02:00')
    expect(component.text()).toContain('50%')
    expect(component.text()).toContain('100%')
  })

  it('passes the save handler through to editable cells', async () => {
    const onSave = vi.fn().mockResolvedValue(undefined)
    const component = await mountSuspended(SettingsAllowanceTable, {
      props: { rows, onSave }
    })

    const buttons = component.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)

    await buttons[0]!.trigger('click')

    const input = component.find('input')
    expect(input.exists()).toBe(true)

    await input.setValue('75')
    await input.trigger('keyup.enter')
    await component.vm.$nextTick()

    expect(onSave).toHaveBeenCalledWith(1, 75)
  })
})
