export interface HolidayResponse {
  date: string // ISO-8601 LocalDate e.g. "2025-06-04"
  name: string | null
}

export interface HolidaySuggestionItem {
  date: string
  name: string | null
}

export interface HolidayInput {
  date: string
  name: string
  selected: boolean
}
