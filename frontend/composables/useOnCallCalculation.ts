export const useOnCallCalculation = () => {
  const oncallStore = useOnCallStore()
  const toast = useToast()

  async function calculate(periodId: number): Promise<boolean> {
    try {
      await oncallStore.calculateDayEntries(periodId)
      toast.add({ title: 'Calculation complete', color: 'success' })
      return true
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : 'Calculation failed'
      toast.add({ title: 'Calculation failed', description: msg, color: 'error' })
      return false
    }
  }

  return { calculate }
}
