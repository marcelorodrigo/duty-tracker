import { defineStore } from 'pinia'
import type { OnCallDayEntry, OvertimeEntry } from './oncall'

export interface RegistrationSummary {
  id: number
  label: string
  periodStart: string
  periodEnd: string
  createdAt: string
  updatedAt: string
}

export interface RegistrationSummaryDetail extends RegistrationSummary {
  onCallEntries: OnCallDayEntry[]
  overtimeEntries: OvertimeEntry[]
}

export const useReportStore = defineStore('report', () => {
  const summaries = ref<RegistrationSummary[]>([])
  const currentSummary = ref<RegistrationSummaryDetail | null>(null)
  const api = useApi()

  async function fetchSummaries() {
    const res = await api.get<{ summaries: RegistrationSummary[] }>('/summaries')
    summaries.value = res.summaries
  }

  async function createSummary(data: { periodId: number; label?: string }) {
    const summary = await api.post<RegistrationSummary>('/summaries', data)
    summaries.value.unshift(summary)
    return summary
  }

  async function deleteSummary(id: number) {
    await api.del(`/summaries/${id}`)
    summaries.value = summaries.value.filter(s => s.id !== id)
  }

  async function fetchSummary(id: number) {
    currentSummary.value = await api.get<RegistrationSummaryDetail>(`/summaries/${id}`)
  }

  async function overrideOnCallEntry(
    summaryId: number,
    entryId: number,
    data: Partial<{ hours: string; rateType: string }>
  ) {
    const entry = await api.put<OnCallDayEntry>(
      `/summaries/${summaryId}/oncall-entries/${entryId}`,
      data
    )
    if (currentSummary.value) {
      const idx = currentSummary.value.onCallEntries.findIndex(e => e.id === entryId)
      if (idx >= 0) currentSummary.value.onCallEntries[idx] = entry
    }
    return entry
  }

  async function deleteOnCallEntry(summaryId: number, entryId: number) {
    await api.del(`/summaries/${summaryId}/oncall-entries/${entryId}`)
    if (currentSummary.value) {
      currentSummary.value.onCallEntries = currentSummary.value.onCallEntries.filter(
        e => e.id !== entryId
      )
    }
  }

  async function addOnCallEntry(
    summaryId: number,
    data: { date: string; hours: string; rateType: string }
  ) {
    const entry = await api.post<OnCallDayEntry>(`/summaries/${summaryId}/oncall-entries`, data)
    if (currentSummary.value) {
      currentSummary.value.onCallEntries.push(entry)
    }
    return entry
  }

  async function overrideOvertimeEntry(
    summaryId: number,
    entryId: number,
    data: Partial<{ overtimeHours: string; allowanceHours: string; allowancePercentage: string }>
  ) {
    const entry = await api.put<OvertimeEntry>(
      `/summaries/${summaryId}/overtime-entries/${entryId}`,
      data
    )
    if (currentSummary.value) {
      const idx = currentSummary.value.overtimeEntries.findIndex(e => e.id === entryId)
      if (idx >= 0) currentSummary.value.overtimeEntries[idx] = entry
    }
    return entry
  }

  async function deleteOvertimeEntry(summaryId: number, entryId: number) {
    await api.del(`/summaries/${summaryId}/overtime-entries/${entryId}`)
    if (currentSummary.value) {
      currentSummary.value.overtimeEntries = currentSummary.value.overtimeEntries.filter(
        e => e.id !== entryId
      )
    }
  }

  async function addOvertimeEntry(
    summaryId: number,
    data: {
      incidentId: number
      overtimeHours: string
      allowanceHours?: string
      allowancePercentage?: string
      timeFrom?: string
      timeTo?: string
      isAllowanceEntry: boolean
    }
  ) {
    const entry = await api.post<OvertimeEntry>(`/summaries/${summaryId}/overtime-entries`, data)
    if (currentSummary.value) {
      currentSummary.value.overtimeEntries.push(entry)
    }
    return entry
  }

  return {
    summaries, currentSummary,
    fetchSummaries, createSummary, deleteSummary, fetchSummary,
    overrideOnCallEntry, deleteOnCallEntry, addOnCallEntry,
    overrideOvertimeEntry, deleteOvertimeEntry, addOvertimeEntry,
  }
})
