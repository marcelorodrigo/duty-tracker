import { describe, expect, it } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import SettingsAllowanceCell from '~/components/settings/AllowanceCell.vue'
import type { AllowanceSaveState, DayTypeCell } from '~/types/compensation'

describe('SettingsAllowanceCell', () => {
  const mockCell: DayTypeCell = {
    id: 1,
    percentage: 50,
    label: 'Test Rate'
  }
  const idle: AllowanceSaveState = { status: 'idle' }

  async function mountCell(saveState: AllowanceSaveState = idle) {
    return mountSuspended(SettingsAllowanceCell, {
      props: { cell: mockCell, saveState }
    })
  }

  async function startEdit(component: Awaited<ReturnType<typeof mountCell>>) {
    await component.find('button').trigger('click')
    return component.find('input')
  }

  it('opens an editor initialized from the displayed percentage', async () => {
    const component = await mountCell()

    expect(component.find('button').text()).toContain('50%')

    const input = await startEdit(component)

    expect(input.element.value).toBe('50')
    expect(input.attributes('aria-label')).toBe('Allowance percentage')
  })

  it('submits a valid draft once with Enter', async () => {
    const component = await mountCell()
    const input = await startEdit(component)
    await input.setValue('75')

    await input.trigger('keydown.enter')
    await flushPromises()
    await input.trigger('keydown.enter')
    await input.trigger('blur')

    expect(component.emitted('save')).toEqual([[{ id: 1, percentage: 75 }]])
  })

  it('submits with Tab and ignores the following blur', async () => {
    const component = await mountCell()
    const input = await startEdit(component)
    await input.setValue('65')

    await input.trigger('keydown.tab')
    await input.trigger('blur')
    await flushPromises()

    expect(component.emitted('save')).toEqual([[{ id: 1, percentage: 65 }]])
  })

  it('cancels an unsubmitted draft on blur', async () => {
    const component = await mountCell()
    const input = await startEdit(component)
    await input.setValue('75')

    await input.trigger('blur')

    expect(component.emitted('save')).toBeUndefined()
    expect(component.find('button').text()).toContain('50%')
  })

  it('cancels an unsubmitted draft on Escape', async () => {
    const component = await mountCell()
    const input = await startEdit(component)
    await input.setValue('75')

    await input.trigger('keyup.escape')
    await flushPromises()

    expect(component.emitted('save')).toBeUndefined()
    expect(component.find('button').text()).toContain('50%')
  })

  it('keeps an invalid draft open and associates its validation error', async () => {
    const component = await mountCell()
    const input = await startEdit(component)
    await input.setValue('125')

    await input.trigger('keydown.enter')
    await flushPromises()

    expect(component.emitted('save')).toBeUndefined()
    expect(component.find('input').element.value).toBe('125')
    expect(component.text()).toContain('Percentage must be 100 or less')
    expect(component.find('input').attributes('aria-invalid')).toBe('true')
  })

  it('locks a slow save without losing the draft or submitting twice', async () => {
    const component = await mountCell()
    const input = await startEdit(component)
    await input.setValue('75')

    await input.trigger('keydown.enter')
    await flushPromises()
    await component.setProps({ saveState: { status: 'saving' } })
    await input.trigger('keydown.tab')
    await input.trigger('blur')

    expect(component.emitted('save')).toEqual([[{ id: 1, percentage: 75 }]])
    expect(component.find('input').element.value).toBe('75')
    expect(component.find('input').attributes('disabled')).toBeDefined()
  })

  it('returns to display mode after a successful save', async () => {
    const component = await mountCell()
    const input = await startEdit(component)
    await input.setValue('75')
    await input.trigger('keydown.enter')
    await flushPromises()

    await component.setProps({
      cell: { ...mockCell, percentage: 75 },
      saveState: { status: 'saving' }
    })
    await component.setProps({ saveState: { status: 'idle' } })

    expect(component.find('input').exists()).toBe(false)
    expect(component.find('button').text()).toContain('75%')
  })

  it('restores the rejected draft for correction and retry', async () => {
    const component = await mountCell()
    const input = await startEdit(component)
    await input.setValue('75')
    await input.trigger('keydown.enter')
    await flushPromises()

    await component.setProps({ saveState: { status: 'saving' } })
    await component.setProps({
      saveState: { status: 'rejected', message: 'Save rejected. Try again.' }
    })

    expect(component.find('input').element.value).toBe('75')
    expect(component.text()).toContain('Save rejected. Try again.')

    await component.find('input').trigger('keydown.enter')
    await flushPromises()
    expect(component.emitted('save')).toEqual([
      [{ id: 1, percentage: 75 }],
      [{ id: 1, percentage: 75 }]
    ])
  })
})
