import { describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import OnCallReportPage from '~/pages/oncall/[id]/report.vue'
import type { OnCallPeriodReportResponse } from '~/types/report'

const mockReport: OnCallPeriodReportResponse = {
  periodId: 1,
  periodStart: '2024-01-15T00:00:00',
  periodEnd: '2024-01-21T23:59:59',
  incidentCount: 0,
  incidentIds: [],
  holidays: [],
  standbyLines: [
    { date: '2024-01-15', dayLabel: 'Monday', hours: '8.0', rateType: 'WEEKDAY_SATURDAY', capped: false },
    { date: '2024-01-16', dayLabel: 'Tuesday', hours: '12.0', rateType: 'WEEKDAY_SATURDAY', capped: true },
  ],
  overtimeLines: [
    { incidentId: 1, incidentName: 'INC-001', date: '2024-01-15', timeFrom: '08:00:00', timeTo: '10:00:00', overtimeHours: '2.0', allowanceHours: null, allowancePercentage: null, isAllowanceEntry: false },
    { incidentId: 2, incidentName: 'INC-002', date: '2024-01-16', timeFrom: '20:00:00', timeTo: '22:00:00', overtimeHours: null, allowanceHours: '2.0', allowancePercentage: '50', isAllowanceEntry: true },
  ],
}

const reportRef = ref<OnCallPeriodReportResponse | null>(mockReport)
const loadingRef = ref(false)
const errorRef = ref<Error | null>(null)

vi.mock('~/composables/useOnCallPeriodReport', () => ({
  useOnCallPeriodReport: () => ({
    report: reportRef,
    loading: loadingRef,
    error: errorRef,
    fetch: () => Promise.resolve(),
  }),
}))

vi.mock('~/composables/useIncidents', () => ({
  useIncidents: () => ({
    fetchById: () => Promise.resolve(null),
  }),
}))

describe('OnCallReportPage - row click completed marking', () => {
  it('toggles completed style on standby row click', async () => {
    const component = await mountSuspended(OnCallReportPage)

    const tables = component.findAll('table')
    expect(tables.length).toBe(2)

    const standbyRows = tables[0]!.findAll('tbody tr')
    expect(standbyRows.length).toBe(2)

    const firstRow = standbyRows[0]!

    await firstRow.trigger('click')
    expect(firstRow.classes()).toContain('line-through')
    expect(firstRow.classes()).toContain('opacity-50')

    await firstRow.trigger('click')
    expect(firstRow.classes()).not.toContain('line-through')
    expect(firstRow.classes()).not.toContain('opacity-50')
  })

  it('toggles completed style on overtime row click', async () => {
    const component = await mountSuspended(OnCallReportPage)

    const tables = component.findAll('table')
    const overtimeRows = tables[1]!.findAll('tbody tr')
    expect(overtimeRows.length).toBe(2)

    const firstRow = overtimeRows[0]!

    await firstRow.trigger('click')
    expect(firstRow.classes()).toContain('line-through')
    expect(firstRow.classes()).toContain('opacity-50')

    await firstRow.trigger('click')
    expect(firstRow.classes()).not.toContain('line-through')
    expect(firstRow.classes()).not.toContain('opacity-50')
  })

  it('keeps standby and overtime selections independent', async () => {
    const component = await mountSuspended(OnCallReportPage)

    const tables = component.findAll('table')
    const standbyRows = tables[0]!.findAll('tbody tr')
    const overtimeRows = tables[1]!.findAll('tbody tr')

    await standbyRows[0]!.trigger('click')
    expect(standbyRows[0]!.classes()).toContain('line-through')
    expect(overtimeRows[0]!.classes()).not.toContain('line-through')
    expect(overtimeRows[1]!.classes()).not.toContain('line-through')

    await overtimeRows[0]!.trigger('click')
    expect(overtimeRows[0]!.classes()).toContain('line-through')
    expect(standbyRows[0]!.classes()).toContain('line-through')
  })
})
