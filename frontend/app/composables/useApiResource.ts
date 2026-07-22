import { readonly, ref } from 'vue'
import type { Ref } from 'vue'
import { ApiProblem } from '~/utils/api'

/**
 * The shared contract for composables that load an API resource.
 *
 * Resources are loaded explicitly through `refresh`. Feature composables may add
 * typed mutation methods, but they keep these four names and state semantics.
 */
export interface ApiResource<T> {
  data: Ref<T | null>
  pending: Readonly<Ref<boolean>>
  error: Readonly<Ref<ApiProblem | null>>
  refresh: () => Promise<void>
}

function normalizeApiProblem(error: unknown, fallbackDetail: string): ApiProblem {
  if (error instanceof ApiProblem) return error

  return new ApiProblem({
    title: 'API request failed',
    detail: error instanceof Error ? error.message : fallbackDetail
  })
}

export function useApiResource<T>(
  load: () => Promise<T>,
  fallbackDetail: string
): ApiResource<T> {
  const data = ref<T | null>(null) as Ref<T | null>
  const pending = ref(false)
  const error = ref<ApiProblem | null>(null)

  async function refresh(): Promise<void> {
    pending.value = true
    error.value = null

    try {
      data.value = await load()
    } catch (cause) {
      error.value = normalizeApiProblem(cause, fallbackDetail)
    } finally {
      pending.value = false
    }
  }

  return {
    data,
    pending: readonly(pending),
    error: readonly(error),
    refresh
  }
}
