import { mountSuspended } from '@nuxt/test-utils/runtime'
import { defineComponent } from 'vue'
import { flushPromises } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { PiniaColada } from '@pinia/colada'

export async function withComposable<T>(factory: () => T): Promise<T> {
  const pinia = createPinia()
  let result!: T
  await mountSuspended(defineComponent({
    setup() {
      result = factory()
      return () => null
    }
  }), {
    global: {
      plugins: [pinia, PiniaColada],
    },
  })
  await flushPromises()
  return result
}
