import * as v from 'valibot'
import { PROFILE_DAYS } from '~/utils/profile'

const optionalHourlyRate = v.nullable(v.pipe(
  v.number('Enter a valid hourly rate'),
  v.check(rate => rate > 1, 'Hourly rate must be greater than 1.00')
))

const optionalStandbyPercentage = (message: string) => v.nullable(v.pipe(
  v.number('Enter a valid standby percentage'),
  v.minValue(0.001, message)
))

export const profileFormSchema = v.object({
  workingDays: v.array(v.picklist(PROFILE_DAYS)),
  workStartTime: v.string(),
  workEndTime: v.string(),
  hourlyRate: optionalHourlyRate,
  standbyWeekdaySaturdayPercentage: optionalStandbyPercentage(
    'Weekday / Saturday percentage must be at least 0.001'
  ),
  standbyWeekdaySundayHolidayPercentage: optionalStandbyPercentage(
    'Sunday / Holiday percentage must be at least 0.001'
  )
})

export type ProfileFormData = v.InferOutput<typeof profileFormSchema>
