import type { OnCallPeriodReportResponse } from '~/types/report'

export function useOnCallPeriodReport(periodId: number) {
  const config = useRuntimeConfig()

  const report = ref<OnCallPeriodReportResponse | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  async function fetch(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const url = `/api/v1/oncall-periods/${periodId}/report`
      const baseURL = config.public.apiBase
      console.log(`Fetching report from: ${baseURL}${url}`)
      report.value = await $fetch<OnCallPeriodReportResponse>(url, {
        baseURL
      })
      console.log('Report loaded successfully:', report.value)
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to generate report'
      console.error(`Error fetching report: ${errorMsg}`, err)
      error.value = err instanceof Error ? err : new Error(errorMsg)
    } finally {
      loading.value = false
    }
  }

  return { report, loading, error, fetch }
}
