export const useApi = () => {
  const config = useRuntimeConfig()
  const toast = useToast()
  const baseUrl = config.public.apiBase as string

  const handleError = (error: unknown) => {
    const problemDetail = (error as { data?: { type?: string; title?: string; detail?: string } }).data
    if (problemDetail) {
      const message = getErrorMessage(problemDetail as ProblemDetail)
      toast.add({ title: problemDetail.title ?? 'Error', description: message, color: 'error' })
    } else {
      toast.add({ title: 'Network Error', description: 'Could not reach the server.', color: 'error' })
    }
    // Mark error as handled to prevent duplicate toast in global error handler
    if (error && typeof error === 'object') {
      (error as any).__handled = true
    }
    throw error
  }

  async function get<T>(path: string): Promise<T> {
    try {
      return await $fetch<T>(`${baseUrl}${path}`)
    } catch (e) {
      return handleError(e) as never
    }
  }

  async function post<T>(path: string, body?: unknown): Promise<T> {
    try {
      return await $fetch<T>(`${baseUrl}${path}`, { method: 'POST', body })
    } catch (e) {
      return handleError(e) as never
    }
  }

  async function put<T>(path: string, body?: unknown): Promise<T> {
    try {
      return await $fetch<T>(`${baseUrl}${path}`, { method: 'PUT', body })
    } catch (e) {
      return handleError(e) as never
    }
  }

  async function del(path: string): Promise<void> {
    try {
      await $fetch(`${baseUrl}${path}`, { method: 'DELETE' })
    } catch (e) {
      return handleError(e) as never
    }
  }

  return { get, post, put, del }
}

export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
}
