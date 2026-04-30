export interface EngineerProfileResponse {
  id: number
  workingDays: string[]
  workStartTime: string
  workEndTime: string
}

export interface UpdateProfileRequest {
  workingDays: string[]
  workStartTime: string
  workEndTime: string
}
