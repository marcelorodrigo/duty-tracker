export interface IncidentResponse {
  id: number
  onCallPeriodId: number
  name: string
  startDateTime: string // ISO-8601 LocalDateTime e.g. "2025-06-03T02:30:00"
  endDateTime: string // ISO-8601 LocalDateTime e.g. "2025-06-03T04:15:00"
  createdAt: string // ISO-8601 Instant
}

export interface CreateIncidentRequest {
  onCallPeriodId: number
  name: string
  startDateTime: string // "2025-06-03T02:30:00"
  endDateTime: string // "2025-06-03T04:15:00"
}

export interface UpdateIncidentRequest {
  name: string
  startDateTime: string
  endDateTime: string
}
