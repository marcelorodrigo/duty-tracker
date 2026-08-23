import { computed } from 'vue'
import { useQuery } from '@pinia/colada'
import { onCallPeriodReportQuery } from '~/queries/onCallPeriodReport'

export function useOnCallPeriodReport(periodId: number) {
  const {
    state: reportState,
    data: report,
    asyncStatus
  } = useQuery(() => ({ ...onCallPeriodReportQuery(periodId) }))

  const pending = computed(() => asyncStatus.value === 'loading')
  const error = computed(() => reportState.value.error as Error | null)

  return { report, pending, error }
}
