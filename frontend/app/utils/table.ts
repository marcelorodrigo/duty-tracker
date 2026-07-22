import type { TableColumn, TableProps, TableRow } from '@nuxt/ui'
import { CAPPED_LABEL } from '~/utils/constants'

const commonTableColumnHeaders = {
  date: 'Date',
  day: 'Day',
  plan: 'Plan',
  option: 'Option',
  hours: 'Hours',
  capped: 'Capped'
} as const

type CommonTableColumnKey = keyof typeof commonTableColumnHeaders
type RowCommonTableColumnKey<Row extends object> = Extract<keyof Row, CommonTableColumnKey>

export function createCommonTableColumns<Row extends object>(
  keys: readonly RowCommonTableColumnKey<Row>[]
): TableColumn<Row>[] {
  return keys.map(accessorKey => ({
    accessorKey,
    header: commonTableColumnHeaders[accessorKey]
  }))
}

export function selectableTableRowClass(row: Pick<TableRow<unknown>, 'getIsSelected'>): string {
  return row.getIsSelected() ? 'cursor-pointer line-through opacity-50' : 'cursor-pointer'
}

export const selectableTableMeta = {
  class: {
    tr: selectableTableRowClass
  }
} satisfies NonNullable<TableProps['meta']>

export function toggleTableRowSelection(
  _event: Event,
  row: Pick<TableRow<unknown>, 'toggleSelected'>
): void {
  row.toggleSelected()
}

export function formatCappedLabel(capped: boolean): string {
  return capped ? CAPPED_LABEL : 'No'
}
