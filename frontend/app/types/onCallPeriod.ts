export interface OnCallPeriodResponse {
  id: number
  startDateTime: string // ISO-8601 LocalDateTime e.g. "2025-06-02T14:00:00"
  endDateTime: string
  holidayOverrides: string[] // ISO-8601 LocalDate e.g. "2025-06-04"
  createdAt: string // ISO-8601 Instant
}

export interface CreateOnCallPeriodRequest {
  startDateTime: string // "2025-06-02T14:00:00"
  endDateTime: string
}

export interface UpdateOnCallPeriodRequest {
  startDateTime: string
  endDateTime: string
}
