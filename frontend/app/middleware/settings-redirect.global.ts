export default defineNuxtRouteMiddleware((to) => {
  if (to.path === '/settings') {
    return navigateTo('/settings/profile', { replace: true })
  }
})
