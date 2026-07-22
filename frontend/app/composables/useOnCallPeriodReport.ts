import type { OnCallPeriodReportResponse } from '~/types/report'

export function useOnCallPeriodReport(periodId: number) {
  const { $api } = useNuxtApp()
  const report = ref<OnCallPeriodReportResponse | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  async function fetch(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      report.value = await $api.get<OnCallPeriodReportResponse>(`/oncall-periods/${periodId}/report`)
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to generate report'
      error.value = err instanceof Error ? err : new Error(errorMsg)
    } finally {
      loading.value = false
    }
  }

  return { report, loading, error, fetch }
}
