import { describe, expect, it, vi } from 'vitest'
import { useAllowanceRateSaves } from '~/composables/useAllowanceRateSaves'

describe('useAllowanceRateSaves', () => {
  it('exposes the pending state until a slow save succeeds', async () => {
    let resolveSave!: (saved: boolean) => void
    const save = vi.fn(() => new Promise<boolean>((resolve) => {
      resolveSave = resolve
    }))
    const { saveRate, saveStates } = useAllowanceRateSaves({ save })

    const savePromise = saveRate({ id: 1, percentage: 75 })

    expect(saveStates.value[1]).toEqual({ status: 'saving' })
    expect(save).toHaveBeenCalledOnce()

    resolveSave(true)
    await savePromise

    expect(saveStates.value[1]).toEqual({ status: 'idle' })
  })

  it('rejects duplicate saves while the same cell is pending', async () => {
    let resolveSave!: (saved: boolean) => void
    const save = vi.fn(() => new Promise<boolean>((resolve) => {
      resolveSave = resolve
    }))
    const { saveRate } = useAllowanceRateSaves({ save })

    const firstSave = saveRate({ id: 1, percentage: 75 })
    await saveRate({ id: 1, percentage: 80 })

    expect(save).toHaveBeenCalledOnce()

    resolveSave(true)
    await firstSave
  })

  it.each([
    ['a rejected result', () => Promise.resolve(false)],
    ['an unexpected exception', () => Promise.reject(new Error('Network error'))]
  ])('makes the draft recoverable after %s', async (_scenario, saveImplementation) => {
    const { saveRate, saveStates } = useAllowanceRateSaves({ save: saveImplementation })

    await saveRate({ id: 1, percentage: 75 })

    expect(saveStates.value[1]).toEqual({
      status: 'rejected',
      message: 'Could not save. Your edit is still available; review it and try again.'
    })
  })
})
