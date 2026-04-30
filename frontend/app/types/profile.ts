export interface EngineerProfileResponse {
  id: number
  employeeType: 'INTERNAL' | 'EXTERNAL'
  workingDays: string[]
  workStartTime: string
  workEndTime: string
  locked: boolean
}
