import type { TableColumn } from '@nuxt/ui'
import { describe, expect, expectTypeOf, it, vi } from 'vitest'
import {
  createCommonTableColumns,
  formatCappedLabel,
  selectableTableMeta,
  selectableTableRowClass,
  toggleTableRowSelection
} from '~/utils/table'

type ExampleRow = {
  date: string
  hours: string
  detail: string
}

describe('table utilities', () => {
  it('creates typed shared columns in the requested order', () => {
    const columns = createCommonTableColumns<ExampleRow>(['date', 'hours'])

    expectTypeOf(columns).toEqualTypeOf<TableColumn<ExampleRow>[]>()
    expect(columns).toEqual([
      { accessorKey: 'date', header: 'Date' },
      { accessorKey: 'hours', header: 'Hours' }
    ])
  })

  it('keeps page-specific columns outside the shared factory', () => {
    const columns: TableColumn<ExampleRow>[] = [
      ...createCommonTableColumns<ExampleRow>(['date']),
      { accessorKey: 'detail', header: 'Detail' },
      ...createCommonTableColumns<ExampleRow>(['hours'])
    ]

    expect(columns).toEqual([
      { accessorKey: 'date', header: 'Date' },
      { accessorKey: 'detail', header: 'Detail' },
      { accessorKey: 'hours', header: 'Hours' }
    ])
  })

  it.each([
    { selected: false, expected: 'cursor-pointer' },
    { selected: true, expected: 'cursor-pointer line-through opacity-50' }
  ])('returns the selectable row class when selected is $selected', ({ selected, expected }) => {
    expect(selectableTableRowClass({ getIsSelected: () => selected })).toBe(expected)
  })

  it('uses the shared row class in the table metadata', () => {
    expect(selectableTableMeta.class?.tr).toBe(selectableTableRowClass)
  })

  it('toggles the selected row', () => {
    const toggleSelected = vi.fn()

    toggleTableRowSelection(new Event('click'), { toggleSelected })

    expect(toggleSelected).toHaveBeenCalledOnce()
  })

  it.each([
    { capped: false, expected: 'No' },
    { capped: true, expected: 'Yes (capped at 15h)' }
  ])('formats the capped label when capped is $capped', ({ capped, expected }) => {
    expect(formatCappedLabel(capped)).toBe(expected)
  })
})
