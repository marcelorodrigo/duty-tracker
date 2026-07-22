import { describe, expect, it } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import SettingsAllowanceCell from '~/components/settings/AllowanceCell.vue'
import type { DayTypeCell } from '~/types/compensation'

describe('SettingsAllowanceCell', () => {
  const mockCell: DayTypeCell = {
    id: 1,
    percentage: 50,
    label: 'Test Rate'
  }

  it('renders percentage as plain text in display mode', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    expect(component.text()).toContain('50%')
  })

  it('enters edit mode on click', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    const displayButton = component.find('button')
    await displayButton.trigger('click')

    // Input should be visible after click
    const input = component.find('input')
    expect(input.exists()).toBe(true)
    expect(input.element.value).toBe('50')
  })

  it('autofocuses the input in edit mode', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    const displayButton = component.find('button')
    await displayButton.trigger('click')

    const input = component.find('input')
    // Input should be focused (check that it exists and has value)
    await component.vm.$nextTick()
    expect(input.exists()).toBe(true)
    expect((input.element as HTMLInputElement).value).toBe('50')
  })

  it('emits a typed save payload when Enter is pressed', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    // Enter edit mode
    await component.find('button').trigger('click')

    // Change value
    const input = component.find('input')
    await input.setValue('75')

    // Press Enter
    await input.trigger('keyup.enter')

    await component.vm.$nextTick()

    expect(component.emitted('save')).toEqual([[{ id: 1, percentage: 75 }]])
  })

  it('reverts to display mode on Enter (optimistic)', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    // Enter edit mode
    await component.find('button').trigger('click')
    const input = component.find('input')
    await input.setValue('75')

    // Press Enter
    await input.trigger('keyup.enter')
    await component.vm.$nextTick()

    // Input should be gone immediately; the parent owns the optimistic update.
    expect(component.find('button').exists()).toBe(true)
  })

  it('cancels edit on Escape without emitting save', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    await component.find('button').trigger('click')
    const input = component.find('input')
    await input.setValue('75')

    // Press Escape
    await input.trigger('keyup.escape')
    await component.vm.$nextTick()

    expect(component.emitted('save')).toBeUndefined()
    // Display mode should be back (showing original 50%)
    expect(component.find('button').text()).toContain('50%')
  })

  it('cancels edit on blur without emitting save', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    await component.find('button').trigger('click')
    const input = component.find('input')
    await input.setValue('75')

    // Blur the input
    await input.trigger('blur')
    await component.vm.$nextTick()

    expect(component.emitted('save')).toBeUndefined()
    // Display mode should be back
    expect(component.find('button').text()).toContain('50%')
  })

  it('rejects invalid input (non-numeric) and cancels', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    await component.find('button').trigger('click')
    const input = component.find('input')
    await input.setValue('abc')

    // Try to confirm
    await input.trigger('keyup.enter')
    await component.vm.$nextTick()

    expect(component.emitted('save')).toBeUndefined()
    // Should return to display mode
    expect(component.find('button').text()).toContain('50%')
  })

  it('rejects input < 0 and cancels', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    await component.find('button').trigger('click')
    const input = component.find('input')
    await input.setValue('-5')

    await input.trigger('keyup.enter')
    await component.vm.$nextTick()

    expect(component.emitted('save')).toBeUndefined()
    expect(component.find('button').text()).toContain('50%')
  })

  it('rejects input > 100 and cancels', async () => {
    const component = await mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell }
    })

    await component.find('button').trigger('click')
    const input = component.find('input')
    await input.setValue('150')

    await input.trigger('keyup.enter')
    await component.vm.$nextTick()

    expect(component.emitted('save')).toBeUndefined()
    expect(component.find('button').text()).toContain('50%')
  })

  it('accepts valid input (0 to 100)', async () => {
    for (const value of [0, 50, 100]) {
      // Create fresh component for each test
      const component = await mountSuspended(SettingsAllowanceCell, {
        props: { cell: mockCell }
      })

      await component.find('button').trigger('click')
      const input = component.find('input')
      await input.setValue(String(value))

      await input.trigger('keyup.enter')
      await component.vm.$nextTick()

      expect(component.emitted('save')).toEqual([[{ id: 1, percentage: value }]])
    }
  })
})
