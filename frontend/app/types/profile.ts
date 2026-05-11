export interface EngineerProfileResponse {
  id: number
  workingDays: string[]
  workStartTime: string
  workEndTime: string
  hourlyRate: number
}

export interface UpdateProfileRequest {
  workingDays: string[]
  workStartTime: string
  workEndTime: string
  hourlyRate?: number
}
