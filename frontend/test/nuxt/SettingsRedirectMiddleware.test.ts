import type { RouteLocationNormalized } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import settingsRedirectMiddleware from '~/middleware/settings-redirect.global'

const mockNavigateTo = vi.fn()

vi.mock('#app/composables/router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('#app/composables/router')>()

  return {
    ...actual,
    navigateTo: (...args: Parameters<typeof actual.navigateTo>) => mockNavigateTo(...args)
  }
})

const route = (path: string) => ({ path }) as RouteLocationNormalized

describe('settings redirect middleware', () => {
  beforeEach(() => {
    mockNavigateTo.mockReset()
  })

  it('replaces the settings route with the profile route', () => {
    settingsRedirectMiddleware(route('/settings'), route('/'))

    expect(mockNavigateTo).toHaveBeenCalledWith('/settings/profile', { replace: true })
  })

  it('leaves settings child routes unchanged', () => {
    const result = settingsRedirectMiddleware(route('/settings/allowance'), route('/settings'))

    expect(result).toBeUndefined()
    expect(mockNavigateTo).not.toHaveBeenCalled()
  })
})
