export interface IncidentResponse {
  id: number
  onCallPeriodId: number
  name: string
  date: string // ISO-8601 LocalDate e.g. "2025-06-03"
  startTime: string // ISO-8601 LocalTime e.g. "02:30:00"
  endTime: string // ISO-8601 LocalTime e.g. "04:15:00"
  createdAt: string // ISO-8601 Instant
}

export interface CreateIncidentRequest {
  onCallPeriodId: number
  name: string
  date: string // "2025-06-03"
  startTime: string // "02:30:00"
  endTime: string // "04:15:00"
}

export interface UpdateIncidentRequest {
  name: string
  date: string
  startTime: string
  endTime: string
}
