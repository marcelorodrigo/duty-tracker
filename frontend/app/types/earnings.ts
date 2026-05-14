export interface StandbyEarningLineResponse {
  date: string // ISO-8601 LocalDate
  dayLabel: string
  compensationLabel: string
  hours: number // BigDecimal
  amount: number // BigDecimal
  capped: boolean
}

export interface IncidentEarningLineResponse {
  incidentId: number
  incidentName: string
  hoursSummary: string // e.g. "3h overtime + 2h 50% allowance + 1h 35% allowance"
  subtotal: number // BigDecimal
}

export interface EarningsResponse {
  periodId: number
  periodStart: string // ISO-8601 LocalDateTime
  periodEnd: string // ISO-8601 LocalDateTime
  standbyLines: StandbyEarningLineResponse[]
  incidentLines: IncidentEarningLineResponse[]
  grandTotal: number // BigDecimal
}
