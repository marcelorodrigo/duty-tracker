import type { HolidayResponse } from '~/types/holiday'

export interface OnCallPeriodResponse {
  id: number
  startDateTime: string // ISO-8601 LocalDateTime e.g. "2025-06-02T14:00:00"
  endDateTime: string
  holidays: HolidayResponse[]
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
