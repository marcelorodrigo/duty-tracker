export interface StandbyEarningLineResponse {
  date: string // ISO-8601 LocalDate
  dayLabel: string
  compensationLabel: string
  hours: string // BigDecimal as string for precision
  amount: string // BigDecimal as string for precision
  capped: boolean
}

export interface IncidentEarningLineResponse {
  incidentId: number
  incidentName: string
  hoursSummary: string // e.g. "3h overtime + 2h 50% allowance + 1h 35% allowance"
  subtotal: string // BigDecimal as string for precision
}

export interface EarningsResponse {
  periodId: number
  periodStart: string // ISO-8601 LocalDateTime
  periodEnd: string // ISO-8601 LocalDateTime
  standbyLines: StandbyEarningLineResponse[]
  incidentLines: IncidentEarningLineResponse[]
  grandTotal: string // BigDecimal as string for precision
}
