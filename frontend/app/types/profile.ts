export interface EngineerProfileResponse {
  id: number
  employeeType: 'INTERNAL' | 'EXTERNAL'
  workingDays: string[]
  workStartTime: string
  workEndTime: string
}

export interface UpdateProfileRequest {
  employeeType: 'INTERNAL' | 'EXTERNAL'
  workingDays: string[]
  workStartTime: string
  workEndTime: string
}
