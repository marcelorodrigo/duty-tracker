import { computed } from 'vue'
import { useQuery } from '@pinia/colada'
import { earningsQuery } from '~/queries/earnings'

export function useEarnings(periodId: number) {
  const {
    state: earningsState,
    data: earnings,
    asyncStatus
  } = useQuery(() => ({ ...earningsQuery(periodId) }))

  const pending = computed(() => asyncStatus.value === 'loading')
  const error = computed(() => earningsState.value.error as Error | null)

  return { earnings, pending, error }
}
