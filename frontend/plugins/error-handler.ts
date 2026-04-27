export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.config.errorHandler = (error, _instance, info) => {
    // Skip errors already handled elsewhere (e.g., by useApi)
    if (error && typeof error === 'object' && (error as any).__handled) {
      console.error('[Global Error]', error, info)
      return
    }

    const toast = useToast()
    console.error('[Global Error]', error, info)
    let message = 'An unexpected error occurred.'
    if (error && typeof error === 'object' && 'message' in error) {
      message = String((error as { message: string }).message)
    }
    toast.add({
      title: 'Error',
      description: message,
      color: 'error',
    })
  }
})
