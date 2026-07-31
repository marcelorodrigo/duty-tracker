export interface EngineerProfileResponse {
  id: number
  workingDays: string[]
  workStartTime: string
  workEndTime: string
  hourlyRate: number
  standbyWeekdaySaturdayPercentage: number
  standbyWeekdaySundayHolidayPercentage: number
  calendarFeedUrl?: string
}

export interface UpdateProfileRequest {
  workingDays: string[]
  workStartTime: string
  workEndTime: string
  hourlyRate?: number
  standbyWeekdaySaturdayPercentage?: number
  standbyWeekdaySundayHolidayPercentage?: number
  calendarFeedUrl?: string
}
