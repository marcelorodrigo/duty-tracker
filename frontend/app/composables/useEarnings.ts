import type { EarningsResponse } from '~/types/earnings'

export function useEarnings(periodId: number) {
  const config = useRuntimeConfig()

  const earnings = ref<EarningsResponse | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  async function fetch(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const url = `/api/v1/oncall-periods/${periodId}/earnings`
      const baseURL = config.public.apiBase
      earnings.value = await $fetch<EarningsResponse>(url, {
        baseURL
      })
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to load earnings'
      error.value = err instanceof Error ? err : new Error(errorMsg)
    } finally {
      loading.value = false
    }
  }

  return { earnings, loading, error, fetch }
}
