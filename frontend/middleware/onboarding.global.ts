export default defineNuxtRouteMiddleware(async (to) => {
  if (to.path === '/onboarding') return

  const api = useApi()
  try {
    const status = await api.get<{ step: string; completed: boolean }>('/onboarding/status')
    if (!status.completed) {
      return navigateTo('/onboarding')
    }
  } catch {
    return navigateTo('/onboarding')
  }
})
