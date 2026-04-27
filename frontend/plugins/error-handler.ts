export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.config.errorHandler = (error, _instance, info) => {
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
