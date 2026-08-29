import { computed, onBeforeUnmount, onMounted, ref, watch, type Ref } from 'vue'
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

  // Monotonic counter that identifies the current "logical" list. Every reset,
  // filter change, or removal bumps it so that responses arriving from a
  // superseded request are discarded instead of corrupting the visible list.
  let requestGeneration = 0
  // Generation of the request currently in flight, or -1 when idle.
  let inFlightGeneration = -1
  // Set when a newer-generation loadNext is blocked by an in-flight older request,
  // so a single reload is fired once the in-flight request settles.
  let reloadQueued = false
  let observer: IntersectionObserver | null = null

  const hasMore = computed(() => !initialized.value || loadedPages.value < totalPages.value)

  async function loadNext(): Promise<void> {
    if (!hasMore.value) {
      return
    }
    if (pending.value) {
      // A request is already in flight. If it belongs to an older generation
      // (e.g. a reset happened), remember to reload for the current one once it settles.
      if (requestGeneration !== inFlightGeneration) {
        reloadQueued = true
      }
      return
    }
    pending.value = true
    error.value = null
    const generation = requestGeneration
    inFlightGeneration = generation
    try {
      await appendPage(generation)
    } catch (e) {
      if (generation === requestGeneration) {
        error.value = e instanceof Error ? e : new Error('Failed to load')
      }
    } finally {
      await settle()
    }
  }

  async function appendPage(generation: number): Promise<void> {
    const page = await options.fetchPage(loadedPages.value, size)
    if (generation !== requestGeneration) {
      return
    }
    const seen = new Set(items.value.map(getKey))
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
  }

  // Releases the in-flight slot and fires a queued reload once settled. This runs
  // for every request (even superseded ones) so the in-flight slot is always freed.
  async function settle(): Promise<void> {
    pending.value = false
    inFlightGeneration = -1
    if (reloadQueued) {
      reloadQueued = false
      await loadNext()
    }
  }

  function reset(): void {
    // Bump so any in-flight request is treated as superseded. Do not clear
    // `pending`: it stays owned by the in-flight request until it settles.
    requestGeneration++
    items.value = []
    totalPages.value = 0
    loadedPages.value = 0
    initialized.value = false
    error.value = null
  }

  function observeSentinel(): void {
    if (observer) {
      observer.disconnect()
      observer = null
    }
    if (sentinelRef.value && typeof IntersectionObserver !== 'undefined') {
      observer = new IntersectionObserver(
        (entries) => {
          if (entries.some(entry => entry.isIntersecting)) {
            void loadNext()
          }
        },
        { rootMargin: '200px' }
      )
      observer.observe(sentinelRef.value)
    }
  }

  watch(sentinelRef, () => {
    observeSentinel()
  })

  onMounted(() => {
    void loadNext()
  })

  onBeforeUnmount(() => {
    if (observer) {
      observer.disconnect()
      observer = null
    }
  })

  return { items, pending, error, hasMore, loadNext, reset, sentinelRef }
}
