export interface IncidentSummaryResponse {
  incidentId: number
  name: string
  date: string // ISO-8601 LocalDate e.g. "2025-04-15"
  startTime: string // ISO-8601 LocalTime e.g. "22:00:00"
  endTime: string
  totalOvertimeHours: string // BigDecimal as string
}

export interface OnCallDayEntryResponse {
  date: string // ISO-8601 LocalDate
  hours: string // BigDecimal as string
  rateType: 'WEEKDAY_SATURDAY' | 'SUNDAY_HOLIDAY'
  capped: boolean
}

export interface ReportOvertimeEntryResponse {
  incidentId: number
  incidentName: string
  date: string // ISO-8601 LocalDate
  timeFrom: string // ISO-8601 LocalTime
  timeTo: string // ISO-8601 LocalTime
  overtimeHours: string | null // BigDecimal as string
  allowanceHours: string | null // BigDecimal as string
  allowancePercentage: string | null // BigDecimal as string
  isAllowanceEntry: boolean
}

export interface OnCallPeriodReportResponse {
  periodId: number
  periodStart: string // ISO-8601 LocalDateTime
  periodEnd: string // ISO-8601 LocalDateTime
  incidentCount: number
  incidentSummaries: IncidentSummaryResponse[]
  standbyLines: OnCallDayEntryResponse[]
  overtimeLines: ReportOvertimeEntryResponse[]
}
