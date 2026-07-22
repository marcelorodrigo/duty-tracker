import { createApiClient } from '~/utils/api'
import type { ApiTransportOptions } from '~/utils/api'

export default defineNuxtPlugin(() => {
  const api = createApiClient(async <TResponse>(request: string, options: ApiTransportOptions) => {
    return await $fetch<TResponse>(
      request,
      options as NonNullable<Parameters<typeof $fetch>[1]>
    )
  })

  return {
    provide: { api }
  }
})
