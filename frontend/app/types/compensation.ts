export type EmployeeType = 'INTERNAL' | 'EXTERNAL'
export type OvertimeDayType = 'WEEKDAY' | 'SATURDAY' | 'SUNDAY_HOLIDAY'
export type RateCategory = 'OVERTIME_ALLOWANCE' | 'OVERTIME_BASE' | 'ONCALL_WEEKDAY_SATURDAY' | 'ONCALL_SUNDAY_HOLIDAY'

export interface CompensationRateResponse {
  id: number
  employeeType: EmployeeType
  rateCategory: RateCategory
  overtimeDayType: OvertimeDayType
  label: string
  timeFrom: string
  timeTo: string
  percentage: number
}

export interface DayTypeCell {
  id: number
  percentage: number
  label: string
}

export interface PivotRow {
  slot: string
  timeFrom: string
  weekday: DayTypeCell
  saturday: DayTypeCell
  sundayHoliday: DayTypeCell
}
