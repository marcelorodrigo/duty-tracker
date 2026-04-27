import { defineStore } from 'pinia'

export interface OnCallPeriod {
  id: number
  startDateTime: string
  endDateTime: string
  holidayOverrides: string[]
  createdAt: string
}

export interface OnCallDayEntry {
  id: number
  date: string
  hours: string
  rateType: 'WEEKDAY_SATURDAY' | 'SUNDAY_HOLIDAY'
  capped: boolean
  timeForTimeFlag: boolean
  manualOverride: boolean
}

export interface Incident {
  id: number
  onCallPeriodId: number | null
  date: string
  startTime: string
  endTime: string
  createdAt: string
}

export interface OvertimeEntry {
  id: number
  incidentId?: number
  overtimeHours: string | null
  allowanceHours: string | null
  allowancePercentage: string | null
  timeFrom: string | null
  timeTo: string | null
  isAllowanceEntry: boolean
  manualOverride: boolean
}

export const useOnCallStore = defineStore('oncall', () => {
  const periods = ref<OnCallPeriod[]>([])
  const currentPeriod = ref<OnCallPeriod | null>(null)
  const dayEntries = ref<OnCallDayEntry[]>([])
  const incidents = ref<Incident[]>([])
  const overtimeEntries = ref<Record<number, OvertimeEntry[]>>({})
  const api = useApi()

  // Periods
  async function fetchPeriods() {
    const res = await api.get<{ periods: OnCallPeriod[] }>('/oncall-periods')
    periods.value = res.periods
  }

  async function fetchPeriod(id: number) {
    currentPeriod.value = await api.get<OnCallPeriod>(`/oncall-periods/${id}`)
  }

  async function createPeriod(data: { startDateTime: string; endDateTime: string }) {
    const period = await api.post<OnCallPeriod>('/oncall-periods', data)
    periods.value.unshift(period)
    return period
  }

  async function updatePeriod(id: number, data: { startDateTime: string; endDateTime: string }) {
    const period = await api.put<OnCallPeriod>(`/oncall-periods/${id}`, data)
    const idx = periods.value.findIndex(p => p.id === id)
    if (idx >= 0) periods.value[idx] = period
    if (currentPeriod.value?.id === id) currentPeriod.value = period
    return period
  }

  async function deletePeriod(id: number) {
    await api.del(`/oncall-periods/${id}`)
    periods.value = periods.value.filter(p => p.id !== id)
  }

  // Holidays
  async function addHoliday(periodId: number, date: string) {
    const period = await api.post<OnCallPeriod>(`/oncall-periods/${periodId}/holidays`, { date })
    if (currentPeriod.value?.id === periodId) currentPeriod.value = period
    return period
  }

  async function removeHoliday(periodId: number, date: string) {
    await api.del(`/oncall-periods/${periodId}/holidays/${date}`)
    if (currentPeriod.value?.id === periodId) {
      currentPeriod.value = {
        ...currentPeriod.value,
        holidayOverrides: currentPeriod.value.holidayOverrides.filter(d => d !== date),
      }
    }
  }

  // Day entries calculation
  async function calculateDayEntries(periodId: number) {
    const res = await api.post<{ periodId: number; entries: OnCallDayEntry[] }>(
      `/oncall-periods/${periodId}/calculate`
    )
    dayEntries.value = res.entries
    return res.entries
  }

  async function fetchDayEntries(periodId: number) {
    // Day entries are loaded via calculate; this is a no-op placeholder
    dayEntries.value = []
  }

  async function overrideDayEntry(
    periodId: number,
    entryId: number,
    data: Partial<{ hours: string; rateType: string; timeForTimeFlag: boolean }>
  ) {
    const entry = await api.put<OnCallDayEntry>(
      `/oncall-periods/${periodId}/day-entries/${entryId}`,
      data
    )
    const idx = dayEntries.value.findIndex(e => e.id === entryId)
    if (idx >= 0) dayEntries.value[idx] = entry
    return entry
  }

  // Incidents
  async function logIncident(data: {
    onCallPeriodId: number | null
    date: string
    startTime: string
    endTime: string
  }) {
    const incident = await api.post<Incident>('/incidents', data)
    incidents.value.push(incident)
    return incident
  }

  async function updateIncident(
    id: number,
    data: { date: string; startTime: string; endTime: string }
  ) {
    const incident = await api.put<Incident>(`/incidents/${id}`, data)
    const idx = incidents.value.findIndex(i => i.id === id)
    if (idx >= 0) incidents.value[idx] = incident
    return incident
  }

  async function deleteIncident(id: number) {
    await api.del(`/incidents/${id}`)
    incidents.value = incidents.value.filter(i => i.id !== id)
  }

  async function listIncidents(onCallPeriodId?: number) {
    const path = onCallPeriodId
      ? `/incidents?onCallPeriodId=${onCallPeriodId}`
      : '/incidents'
    const res = await api.get<{ incidents: Incident[] }>(path)
    incidents.value = res.incidents
    return res.incidents
  }

  async function calculateOvertimeEntries(incidentId: number) {
    const res = await api.post<{ incidentId: number; entries: OvertimeEntry[] }>(
      `/incidents/${incidentId}/calculate`
    )
    overtimeEntries.value[incidentId] = res.entries
    return res.entries
  }

  // Aliases used by HolidayOverrideList component
  const addHolidayOverride = addHoliday
  const removeHolidayOverride = removeHoliday

  return {
    periods, currentPeriod, dayEntries, incidents, overtimeEntries,
    fetchPeriods, fetchPeriod, createPeriod, updatePeriod, deletePeriod,
    addHoliday, removeHoliday, addHolidayOverride, removeHolidayOverride,
    calculateDayEntries, fetchDayEntries, overrideDayEntry,
    logIncident, updateIncident, deleteIncident, listIncidents, calculateOvertimeEntries,
  }
})
