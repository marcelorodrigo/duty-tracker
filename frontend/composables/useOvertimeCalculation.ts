export const useOvertimeCalculation = () => {
  const oncallStore = useOnCallStore()
  const toast = useToast()

  async function calculate(incidentId: number) {
    try {
      await oncallStore.calculateOvertimeEntries(incidentId)
      toast.add({ title: 'Overtime calculated', color: 'success' })
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : 'Calculation failed'
      if (typeof error === 'object' && error !== null && 'type' in error) {
        const e = error as { type: string }
        if (e.type?.includes('incident-during-working-hours')) {
          toast.add({ title: 'No overtime applicable', description: 'All hours fall within normal working hours.', color: 'warning' })
          return
        }
        if (e.type?.includes('overtime-day-off')) {
          toast.add({ title: 'Day off', description: 'This day is flagged as time-for-time. No overtime pay applies.', color: 'warning' })
          return
        }
      }
      toast.add({ title: 'Calculation failed', description: msg, color: 'error' })
    }
  }

  return { calculate }
}
