import { CalendarDateTime, ZonedDateTime } from '@internationalized/date'
import * as v from 'valibot'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

type IncidentPeriodWindow = Pick<OnCallPeriodResponse, 'startDateTime' | 'endDateTime'>

export type IncidentDateTimeValue = CalendarDateTime | ZonedDateTime

const incidentDateTime = (message: string) => v.union([
  v.instance(CalendarDateTime, message),
  v.instance(ZonedDateTime, message)
], message)

export function toIncidentDateTimeString(dateTime: IncidentDateTimeValue): string {
  const month = String(dateTime.month).padStart(2, '0')
  const day = String(dateTime.day).padStart(2, '0')
  const hour = String(dateTime.hour).padStart(2, '0')
  const minute = String(dateTime.minute).padStart(2, '0')
  return `${dateTime.year}-${month}-${day}T${hour}:${minute}:00`
}

export function isIncidentStartWithinPeriod(
  startDateTime: IncidentDateTimeValue,
  period: IncidentPeriodWindow
): boolean {
  const start = new Date(toIncidentDateTimeString(startDateTime))
  return start >= new Date(period.startDateTime) && start <= new Date(period.endDateTime)
}

export function isIncidentEndAfterPeriod(
  endDateTime: IncidentDateTimeValue,
  period: IncidentPeriodWindow
): boolean {
  return new Date(toIncidentDateTimeString(endDateTime)) > new Date(period.endDateTime)
}

export function createIncidentFormSchema(period: IncidentPeriodWindow) {
  return v.pipe(
    v.object({
      name: v.pipe(
        v.string('Name is required.'),
        v.trim(),
        v.minLength(1, 'Name is required.')
      ),
      startDateTime: incidentDateTime('Start date/time is required.'),
      endDateTime: incidentDateTime('End date/time is required.')
    }),
    v.forward(
      v.check(
        input => isIncidentStartWithinPeriod(input.startDateTime, period),
        'Start date/time must be within the on-call period window.'
      ),
      ['startDateTime']
    )
  )
}

export type IncidentFormData = v.InferOutput<ReturnType<typeof createIncidentFormSchema>>
