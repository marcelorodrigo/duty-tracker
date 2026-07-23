import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import OnCallReportPage from '~/pages/oncall/[id]/report.vue'
import type { OnCallPeriodReportResponse } from '~/types/report'
import { ApiProblem } from '~/utils/api'

const mockReport: OnCallPeriodReportResponse = {
  periodId: 1,
  periodStart: '2024-01-15T00:00:00',
  periodEnd: '2024-01-21T23:59:59',
  incidentCount: 0,
  incidentIds: [],
  holidays: [],
  standbyLines: [
    { date: '2024-01-15', dayLabel: 'Monday', hours: '8.0', rateType: 'WEEKDAY_SATURDAY', capped: false },
    { date: '2024-01-16', dayLabel: 'Tuesday', hours: '12.0', rateType: 'WEEKDAY_SATURDAY', capped: true }
  ],
  overtimeLines: [
    { date: '2024-01-15', isAllowanceEntry: false, allowancePercentage: null, hours: '2.0', incidentIds: [1] },
    { date: '2024-01-16', isAllowanceEntry: true, allowancePercentage: '50', hours: '2.0', incidentIds: [2] }
  ]
}

const reportRef = ref<OnCallPeriodReportResponse | null>(mockReport)
const pendingRef = ref(false)
const errorRef = ref<ApiProblem | null>(null)

vi.mock('~/composables/useOnCallPeriodReport', () => ({
  useOnCallPeriodReport: () => ({
    data: reportRef,
    pending: pendingRef,
    error: errorRef,
    refresh: () => Promise.resolve()
  })
}))

vi.mock('~/composables/useIncidents', () => ({
  useIncidents: () => ({
    data: ref([]),
    pending: ref(false),
    error: ref(null),
    refresh: () => Promise.resolve()
  })
}))

beforeEach(() => {
  reportRef.value = mockReport
  pendingRef.value = false
  errorRef.value = null
})

describe('OnCallReportPage - controlled error messages', () => {
  it.each([
    {
      problem: { status: 400 },
      expected: 'Some information is invalid. Please review it and try again.'
    },
    {
      problem: { status: 404 },
      expected: 'The requested item could not be found.'
    },
    {
      problem: {
        type: 'https://duty-tracker.example/errors/incident-overlap',
        status: 409
      },
      expected: 'This incident overlaps another incident.'
    },
    {
      problem: { status: 500 },
      expected: 'The server could not complete the request. Please try again later.'
    },
    {
      problem: {},
      expected: 'We could not reach the server. Check your connection and try again.'
    }
  ])('renders controlled text for $expected', async ({ problem, expected }) => {
    const arbitraryBackendText = 'SQLSTATE 23505: secret_table_internal_idx'
    errorRef.value = new ApiProblem({ ...problem, detail: arbitraryBackendText })

    const component = await mountSuspended(OnCallReportPage)

    expect(component.text()).toContain(expected)
    expect(component.text()).not.toContain(arbitraryBackendText)
  })
})

describe('OnCallReportPage - overtime table columns', () => {
  it('renders exactly 4 columns in the overtime table: Date, Plan, Option, Hours', async () => {
    const component = await mountSuspended(OnCallReportPage)

    const tables = component.findAll('table')
    const overtimeTable = tables[1]!
    const headers = overtimeTable.findAll('thead th')

    expect(headers).toHaveLength(4)
    const headerTexts = headers.map(h => h.text())
    expect(headerTexts).toContain('Date')
    expect(headerTexts).toContain('Plan')
    expect(headerTexts).toContain('Option')
    expect(headerTexts).toContain('Hours')
    expect(headerTexts).not.toContain('Incident')
    expect(headerTexts).not.toContain('Time')
  })
})

describe('OnCallReportPage - overtime row rendering', () => {
  it('renders non-allowance grouped entry with option label "Overtime hours" and hours value', async () => {
    const component = await mountSuspended(OnCallReportPage)

    const tables = component.findAll('table')
    const overtimeRows = tables[1]!.findAll('tbody tr')
    const firstRow = overtimeRows[0]!
    const cells = firstRow.findAll('td')

    // Option column (index 2) should say "Overtime hours"
    expect(cells[2]!.text()).toBe('Overtime hours')
    // Hours column (index 3) should show the hours value
    expect(cells[3]!.text()).toBe('2.0')
  })

  it('renders allowance grouped entry with option label "{percentage}% allowance" and hours value', async () => {
    const component = await mountSuspended(OnCallReportPage)

    const tables = component.findAll('table')
    const overtimeRows = tables[1]!.findAll('tbody tr')
    const secondRow = overtimeRows[1]!
    const cells = secondRow.findAll('td')

    // Option column (index 2) should say "50% allowance"
    expect(cells[2]!.text()).toBe('50% allowance')
    // Hours column (index 3) should show the hours value
    expect(cells[3]!.text()).toBe('2.0')
  })
})

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
