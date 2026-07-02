import { mountSuspended } from '@nuxt/test-utils/runtime'
import { defineComponent } from 'vue'
import { flushPromises } from '@vue/test-utils'

export async function withComposable<T>(factory: () => T): Promise<T> {
  let result!: T
  await mountSuspended(defineComponent({
    setup() {
      result = factory()
      return () => null
    }
  }))
  await flushPromises()
  return result
}
