export interface CalendarFeedEvent {
  startDateTime: string
  endDateTime: string
  summary: string
}

export interface CalendarFeedPreview {
  upcoming: CalendarFeedEvent[]
  past: CalendarFeedEvent[]
}
