import { computed, onMounted, ref, type Ref } from 'vue'
import type { PageResponse } from '~/types/page'

export interface UseInfiniteListOptions<T> {
  fetchPage: (page: number, size: number) => Promise<PageResponse<T>>
  size?: number
  getKey?: (item: T) => number | string
}

export interface UseInfiniteListResult<T> {
  items: Ref<T[]>
  pending: Ref<boolean>
  error: Ref<Error | null>
  hasMore: Ref<boolean>
  loadNext: () => Promise<void>
  reset: () => void
  sentinelRef: Ref<HTMLElement | null>
}

export function useInfiniteList<T>(options: UseInfiniteListOptions<T>): UseInfiniteListResult<T> {
  const size = options.size ?? 20
  const getKey = options.getKey ?? ((item: T) => (item as unknown as { id: number }).id)

  const items = ref<T[]>([]) as Ref<T[]>
  const totalPages = ref(0)
  const loadedPages = ref(0)
  const pending = ref(false)
  const error = ref<Error | null>(null)
  const initialized = ref(false)
  const sentinelRef = ref<HTMLElement | null>(null)

  const hasMore = computed(() => !initialized.value || loadedPages.value < totalPages.value)

  async function loadNext(): Promise<void> {
    if (pending.value || !hasMore.value) {
      return
    }
    pending.value = true
    error.value = null
    try {
      const page = await options.fetchPage(loadedPages.value, size)
      const seen = new Set(items.value.map(i => getKey(i)))
      for (const item of page.content) {
        const key = getKey(item)
        if (!seen.has(key)) {
          seen.add(key)
          items.value.push(item)
        }
      }
      totalPages.value = page.totalPages
      loadedPages.value = page.page + 1
      initialized.value = true
    } catch (e) {
      error.value = e instanceof Error ? e : new Error('Failed to load')
    } finally {
      pending.value = false
    }
  }

  function reset(): void {
    items.value = []
    totalPages.value = 0
    loadedPages.value = 0
    initialized.value = false
    error.value = null
  }

  onMounted(() => {
    void loadNext()
    if (typeof IntersectionObserver !== 'undefined' && sentinelRef.value) {
      const observer = new IntersectionObserver(
        (entries) => {
          if (entries.some(entry => entry.isIntersecting)) {
            void loadNext()
          }
        },
        { rootMargin: '200px' }
      )
      observer.observe(sentinelRef.value)
    }
  })

  return { items, pending, error, hasMore, loadNext, reset, sentinelRef }
}
