export default defineNuxtRouteMiddleware(async (to) => {
  // Skip middleware for onboarding routes themselves
  if (to.path.startsWith('/onboarding')) return

  const config = useRuntimeConfig()

  // Use session-scoped cache for onboarding status
  const cachedStatus = useState('onboardingStatus', () => ({
    data: null as { step: string; completed: boolean } | null,
  }))

  // Return cached status if available
  if (cachedStatus.value.data !== null) {
    if (!cachedStatus.value.data.completed) {
      return navigateTo('/onboarding')
    }
    return
  }

  // Fetch silently without triggering global toasts
  try {
    const status = await $fetch<{ step: string; completed: boolean }>(`${config.public.apiBase}/onboarding`)
    cachedStatus.value.data = status
    if (!status.completed) {
      return navigateTo('/onboarding')
    }
  } catch {
    // Silent failure: redirect without displaying error toast
    return navigateTo('/onboarding')
  }
})
