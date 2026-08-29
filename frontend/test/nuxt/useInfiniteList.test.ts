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
