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
  incidentIds: number[]
  standbyLines: OnCallDayEntryResponse[]
  overtimeLines: ReportOvertimeEntryResponse[]
}
