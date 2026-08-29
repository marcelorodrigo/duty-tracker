import { describe, it, expect, vi } from 'vitest'
import { defineComponent, h, type Ref } from 'vue'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { flushPromises } from '@vue/test-utils'
import { useInfiniteList, type UseInfiniteListResult } from '~/composables/useInfiniteList'
import type { PageResponse } from '~/types/page'

interface Item {
  id: number
  name: string
}

function page(content: Item[], page: number, totalPages: number): PageResponse<Item> {
  return { content, page, size: content.length, totalElements: 0, totalPages }
}

describe('useInfiniteList', () => {
  it('should accumulate content across pages and expose hasMore while pages remain', async () => {
    const fetchPage = vi.fn(async (p: number) => {
      if (p === 0) {
        return page([{ id: 1, name: 'a' }, { id: 2, name: 'b' }], 0, 2)
      }
      return page([{ id: 3, name: 'c' }], 1, 2)
    })

    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    await list.loadNext()
    expect(list.items.value).toEqual([{ id: 1, name: 'a' }, { id: 2, name: 'b' }])
    expect(list.hasMore.value).toBe(true)

    await list.loadNext()
    expect(list.items.value).toHaveLength(3)
    expect(list.hasMore.value).toBe(false)
  })

  it('should dedupe items by key across pages', async () => {
    const fetchPage = vi.fn(async (p: number) => {
      if (p === 0) {
        return page([{ id: 1, name: 'a' }, { id: 2, name: 'b' }], 0, 2)
      }
      return page([{ id: 2, name: 'b' }, { id: 3, name: 'c' }], 1, 2)
    })

    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    await list.loadNext()
    await list.loadNext()

    expect(list.items.value).toEqual([
      { id: 1, name: 'a' },
      { id: 2, name: 'b' },
      { id: 3, name: 'c' }
    ])
  })

  it('should not fetch again while a request is pending', async () => {
    let resolve!: (value: PageResponse<Item>) => void
    const deferred = new Promise<PageResponse<Item>>((res) => {
      resolve = res
    })
    const fetchPage = vi.fn(() => deferred)

    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    const first = list.loadNext()
    const second = list.loadNext()
    expect(fetchPage).toHaveBeenCalledTimes(1)

    resolve(page([{ id: 1, name: 'a' }], 0, 1))
    await Promise.all([first, second])
  })

  it('should clear items and reset pagination on reset', async () => {
    const fetchPage = vi.fn(async () => page([{ id: 1, name: 'a' }], 0, 1))
    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    await list.loadNext()
    expect(list.items.value).toHaveLength(1)

    list.reset()
    expect(list.items.value).toEqual([])
    expect(list.hasMore.value).toBe(true)
    expect(list.pending.value).toBe(false)
  })

  it('should surface the error when the fetch rejects', async () => {
    const fetchPage = vi.fn(async () => {
      throw new Error('boom')
    })
    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    await list.loadNext()
    expect(list.error.value).toBeInstanceOf(Error)
    expect(list.error.value?.message).toBe('boom')
  })

  it('should wrap a non-Error rejection in a generic Error', async () => {
    const fetchPage = vi.fn(async () => {
      throw 'connection lost'
    })
    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    await list.loadNext()
    expect(list.error.value).toBeInstanceOf(Error)
    expect(list.error.value?.message).toBe('Failed to load')
  })

  it('should request the default page size of 20 when not provided', async () => {
    const fetchPage = vi.fn(async () => page([], 0, 1))
    const list = useInfiniteList<Item>({ fetchPage })

    await list.loadNext()
    expect(fetchPage).toHaveBeenCalledWith(0, 20)
  })

  it('should honour a custom getKey for deduplication', async () => {
    const fetchPage = vi.fn(async (p: number) => {
      if (p === 0) {
        return page([{ id: 1, name: 'a' }, { id: 2, name: 'b' }], 0, 2)
      }
      return page([{ id: 2, name: 'b' }, { id: 3, name: 'c' }], 1, 2)
    })
    const getKey = vi.fn((item: Item) => `key-${item.id}`)

    const list = useInfiniteList<Item>({ fetchPage, size: 2, getKey })

    await list.loadNext()
    await list.loadNext()

    expect(getKey).toHaveBeenCalled()
    expect(list.items.value).toEqual([
      { id: 1, name: 'a' },
      { id: 2, name: 'b' },
      { id: 3, name: 'c' }
    ])
  })

  it('should not fetch again once the last page has been loaded', async () => {
    const fetchPage = vi.fn(async () => page([{ id: 1, name: 'a' }], 0, 1))
    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    await list.loadNext()
    fetchPage.mockClear()
    await list.loadNext()

    expect(fetchPage).not.toHaveBeenCalled()
  })
})

describe('useInfiniteList lifecycle', () => {
  it('auto-loads the first page on mount and loads more when the sentinel intersects', async () => {
    const fetchPage = vi.fn(async (p: number) => {
      if (p === 0) {
        return page([{ id: 1, name: 'a' }], 0, 2)
      }
      return page([{ id: 2, name: 'b' }], 1, 2)
    })
    const observe = vi.fn()
    let callback: IntersectionObserverCallback | undefined
    vi.stubGlobal('IntersectionObserver', class {
      constructor(cb: IntersectionObserverCallback) {
        callback = cb
      }

      observe = observe
      unobserve = vi.fn()
      disconnect = vi.fn()
      takeRecords = vi.fn()
    })

    let api: UseInfiniteListResult<Item> | undefined
    let sentinel: Ref<HTMLElement | null> | undefined
    try {
      await mountSuspended(defineComponent({
        setup() {
          api = useInfiniteList<Item>({ fetchPage, size: 2 })
          sentinel = api.sentinelRef
          return () => h('div', {
            ref: (el: unknown) => {
              if (sentinel) {
                sentinel.value = el as HTMLElement
              }
            }
          })
        }
      }))
      await flushPromises()

      expect(fetchPage).toHaveBeenCalledWith(0, 2)
      expect(observe).toHaveBeenCalledTimes(1)
      expect(api!.items.value).toHaveLength(1)

      fetchPage.mockClear()
      callback!([{ isIntersecting: true } as IntersectionObserverEntry], {} as IntersectionObserver)
      await flushPromises()
      expect(fetchPage).toHaveBeenCalledWith(1, 2)
      expect(api!.items.value).toHaveLength(2)

      fetchPage.mockClear()
      callback!([{ isIntersecting: false } as IntersectionObserverEntry], {} as IntersectionObserver)
      await flushPromises()
      expect(fetchPage).not.toHaveBeenCalled()
    } finally {
      vi.unstubAllGlobals()
    }
  })

  it('does not observe the sentinel when IntersectionObserver is unavailable', async () => {
    vi.stubGlobal('IntersectionObserver', undefined)
    const fetchPage = vi.fn(async () => page([{ id: 1, name: 'a' }], 0, 1))

    let api: UseInfiniteListResult<Item> | undefined
    try {
      await mountSuspended(defineComponent({
        setup() {
          api = useInfiniteList<Item>({ fetchPage })
          return () => h('div', {
            ref: (el: unknown) => {
              api!.sentinelRef.value = el as HTMLElement
            }
          })
        }
      }))
      await flushPromises()

      expect(fetchPage).toHaveBeenCalledWith(0, 20)
    } finally {
      vi.unstubAllGlobals()
    }
  })
})

describe('useInfiniteList staleness', () => {
  it('discards a response that resolves after a reset', async () => {
    let resolveStale!: (value: PageResponse<Item>) => void
    const stale = new Promise<PageResponse<Item>>((res) => {
      resolveStale = res
    })
    const fetchPage = vi.fn()
    fetchPage.mockReturnValueOnce(stale)
    fetchPage.mockResolvedValueOnce(page([{ id: 2, name: 'fresh' }], 0, 1))

    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    const first = list.loadNext()
    expect(fetchPage).toHaveBeenCalledTimes(1)

    // Supersede the in-flight request
    list.reset()
    resolveStale(page([{ id: 1, name: 'stale' }], 0, 1))
    await first

    // Stale data must not leak into the list
    expect(list.items.value).toEqual([])
    expect(list.pending.value).toBe(false)

    // A fresh load applies fresh data
    await list.loadNext()
    expect(list.items.value).toEqual([{ id: 2, name: 'fresh' }])
  })

  it('reloads once the stale in-flight request settles when a newer loadNext is blocked', async () => {
    let resolveStale!: (value: PageResponse<Item>) => void
    const stale = new Promise<PageResponse<Item>>((res) => {
      resolveStale = res
    })
    let resolveReload!: (value: PageResponse<Item>) => void
    const reload = new Promise<PageResponse<Item>>((res) => {
      resolveReload = res
    })
    const fetchPage = vi.fn()
    fetchPage.mockReturnValueOnce(stale)
    fetchPage.mockReturnValueOnce(reload)

    const list = useInfiniteList<Item>({ fetchPage, size: 2 })

    const first = list.loadNext() // in-flight fetch #1
    list.reset() // newer generation
    const blocked = list.loadNext() // blocked by in-flight, queues a reload
    expect(fetchPage).toHaveBeenCalledTimes(1)

    resolveStale(page([{ id: 1, name: 'old' }], 0, 1))
    resolveReload(page([{ id: 2, name: 'new' }], 0, 1))
    await first
    await blocked
    await flushPromises()

    // The queued reload fired for the current generation and old data was discarded
    expect(fetchPage).toHaveBeenCalledTimes(2)
    expect(list.items.value).toEqual([{ id: 2, name: 'new' }])
  })
})

describe('useInfiniteList observer lifecycle', () => {
  it('re-creates the observer when the sentinel element changes', async () => {
    const instances: Array<{ observe: ReturnType<typeof vi.fn>, disconnect: ReturnType<typeof vi.fn> }> = []
    vi.stubGlobal('IntersectionObserver', class {
      constructor(_cb: IntersectionObserverCallback) {
        instances.push(this as unknown as { observe: ReturnType<typeof vi.fn>, disconnect: ReturnType<typeof vi.fn> })
      }

      observe = vi.fn()
      disconnect = vi.fn()
      unobserve = vi.fn()
      takeRecords = vi.fn()
    })

    let api: UseInfiniteListResult<Item> | undefined
    try {
      await mountSuspended(defineComponent({
        setup() {
          api = useInfiniteList<Item>({ fetchPage: async () => page([], 0, 1) })
          return () => h('div', {
            ref: (el: unknown) => {
              if (api) {
                api.sentinelRef.value = el as HTMLElement
              }
            }
          })
        }
      }))
      await flushPromises()

      expect(instances).toHaveLength(1)
      expect(instances[0]!.observe).toHaveBeenCalledTimes(1)

      api!.sentinelRef.value = document.createElement('div')
      await flushPromises()

      expect(instances).toHaveLength(2)
      expect(instances[0]!.disconnect).toHaveBeenCalledTimes(1)
      expect(instances[1]!.observe).toHaveBeenCalledTimes(1)
    } finally {
      vi.unstubAllGlobals()
    }
  })

  it('disconnects the observer on unmount', async () => {
    const instances: Array<{ observe: ReturnType<typeof vi.fn>, disconnect: ReturnType<typeof vi.fn> }> = []
    vi.stubGlobal('IntersectionObserver', class {
      constructor(_cb: IntersectionObserverCallback) {
        instances.push(this as unknown as { observe: ReturnType<typeof vi.fn>, disconnect: ReturnType<typeof vi.fn> })
      }

      observe = vi.fn()
      disconnect = vi.fn()
      unobserve = vi.fn()
      takeRecords = vi.fn()
    })

    let api: UseInfiniteListResult<Item> | undefined
    let wrapper: { unmount: () => void }
    try {
      wrapper = await mountSuspended(defineComponent({
        setup() {
          api = useInfiniteList<Item>({ fetchPage: async () => page([], 0, 1) })
          return () => h('div', {
            ref: (el: unknown) => {
              if (api) {
                api.sentinelRef.value = el as HTMLElement
              }
            }
          })
        }
      }))
      await flushPromises()

      expect(instances).toHaveLength(1)
      wrapper.unmount()
      await flushPromises()

      expect(instances[0]!.disconnect).toHaveBeenCalledTimes(1)
    } finally {
      vi.unstubAllGlobals()
    }
  })
})
