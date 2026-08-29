import { describe, it, expect, vi } from 'vitest'
import { useInfiniteList } from '~/composables/useInfiniteList'
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
})
