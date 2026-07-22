import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import EarningsPage from '~/pages/oncall/[id]/earnings.vue'
import type {
  EarningsResponse,
  IncidentEarningLineResponse,
  StandbyEarningLineResponse
} from '~/types/earnings'
import { buildEarnings } from '../utils/factories'

const earningsRef = ref<EarningsResponse | null>(null)
const refreshMock = vi.fn()

vi.mock('~/composables/useEarnings', () => ({
  useEarnings: () => ({
    data: earningsRef,
    pending: ref(false),
    error: ref(null),
    refresh: refreshMock
  })
}))

function standbyLine(
  amount: string,
  overrides: Partial<StandbyEarningLineResponse> = {}
): StandbyEarningLineResponse {
  return {
    date: '2026-04-01',
    dayLabel: 'Wednesday',
    compensationLabel: 'Weekday standby',
    hours: '8.00',
    amount,
    capped: false,
    ...overrides
  }
}

function incidentLine(
  subtotal: string,
  overrides: Partial<IncidentEarningLineResponse> = {}
): IncidentEarningLineResponse {
  return {
    incidentId: 1,
    incidentName: 'Database failover',
    hoursSummary: '1h overtime',
    subtotal,
    ...overrides
  }
}

interface EarningsScenario {
  name: string
  earnings: EarningsResponse
  standbyTotal: string
  incidentTotal: string
  grandTotal: string
}

const scenarios: EarningsScenario[] = [
  {
    name: 'empty earnings',
    earnings: buildEarnings({ grandTotal: '0.00' }),
    standbyTotal: '€0.00',
    incidentTotal: '€0.00',
    grandTotal: '€0.00'
  },
  {
    name: 'one standby and one incident line',
    earnings: buildEarnings({
      standbyLines: [standbyLine('12.50')],
      incidentLines: [incidentLine('7.25')],
      grandTotal: '19.75'
    }),
    standbyTotal: '€12.50',
    incidentTotal: '€7.25',
    grandTotal: '€19.75'
  },
  {
    name: 'multiple standby and incident lines',
    earnings: buildEarnings({
      standbyLines: [
        standbyLine('10.00'),
        standbyLine('20.00', { date: '2026-04-02', dayLabel: 'Thursday' }),
        standbyLine('30.50', { date: '2026-04-03', dayLabel: 'Friday' })
      ],
      incidentLines: [
        incidentLine('1.25'),
        incidentLine('2.75', { incidentId: 2, incidentName: 'Cache outage' })
      ],
      grandTotal: '64.50'
    }),
    standbyTotal: '€60.50',
    incidentTotal: '€4.00',
    grandTotal: '€64.50'
  },
  {
    name: 'large decimal values',
    earnings: buildEarnings({
      standbyLines: [
        standbyLine('123456789.12'),
        standbyLine('0.88', { date: '2026-04-02', dayLabel: 'Thursday' })
      ],
      incidentLines: [
        incidentLine('987654321.09'),
        incidentLine('0.01', { incidentId: 2, incidentName: 'Network outage' })
      ],
      grandTotal: '1111111111.10'
    }),
    standbyTotal: '€123456790.00',
    incidentTotal: '€987654321.10',
    grandTotal: '€1111111111.10'
  }
]

beforeEach(() => {
  earningsRef.value = null
  refreshMock.mockReset()
})

describe('EarningsPage financial summary', () => {
  it('lays out totals in progressively wider responsive columns', async () => {
    earningsRef.value = buildEarnings({ grandTotal: '0.00' })

    const component = await mountSuspended(EarningsPage, {
      route: '/oncall/42/earnings'
    })

    const totals = component.get('[aria-label="Earnings totals"]')

    expect(totals.classes()).toEqual(expect.arrayContaining([
      'grid-cols-1',
      'sm:grid-cols-2',
      'lg:grid-cols-3'
    ]))
    expect(totals.classes()).not.toContain('grid-cols-3')
  })

  it.each(scenarios)(
    'renders the financial summary totals for $name',
    async ({ earnings, standbyTotal, incidentTotal, grandTotal }) => {
      earningsRef.value = earnings

      const component = await mountSuspended(EarningsPage, {
        route: '/oncall/42/earnings'
      })

      const summary = component.findAll('.grid > div').map((card) => {
        const paragraphs = card.findAll('p')
        return {
          label: paragraphs[0]?.text(),
          value: paragraphs[1]?.text()
        }
      })

      expect(summary).toEqual([
        { label: 'Standby Subtotal', value: standbyTotal },
        { label: 'Incident Subtotal', value: incidentTotal },
        { label: 'Grand Total (bruto)', value: grandTotal }
      ])
    }
  )

  it('renders the grand total returned by the API instead of deriving it from line items', async () => {
    earningsRef.value = buildEarnings({
      standbyLines: [standbyLine('10.00')],
      incidentLines: [incidentLine('5.00')],
      grandTotal: '42.75'
    })

    const component = await mountSuspended(EarningsPage, {
      route: '/oncall/42/earnings'
    })

    const grandTotalCard = component.findAll('.grid > div')[2]
    expect(grandTotalCard?.text()).toContain('Grand Total (bruto)')
    expect(grandTotalCard?.text()).toContain('€42.75')
  })
})
