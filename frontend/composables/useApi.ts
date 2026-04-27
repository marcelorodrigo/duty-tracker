export const useApi = () => {
  const config = useRuntimeConfig()
  const toast = useToast()
  const baseUrl = config.public.apiBase as string

  // Type guard for RFC-7807 ProblemDetail shape
  const isProblemDetail = (obj: any): obj is ProblemDetail => {
    return (
      obj &&
      typeof obj === 'object' &&
      ('title' in obj || 'detail' in obj) &&
      (typeof obj.title === 'string' || obj.title === undefined) &&
      (typeof obj.detail === 'string' || obj.detail === undefined)
    )
  }

  const handleError = (error: unknown) => {
    const data = (error as { data?: any }).data

    if (isProblemDetail(data)) {
      const message = getErrorMessage(data)
      const description = data.status ? `${message} (Status: ${data.status})` : message
      toast.add({ title: data.title ?? 'Error', description, color: 'error' })
    } else {
      // Try to extract HTTP status from FetchError
      const statusCode = (error as any).statusCode || (error as any).status
      const description = statusCode
        ? `Could not reach the server. (Status: ${statusCode})`
        : 'Could not reach the server.'
      toast.add({ title: 'Network Error', description, color: 'error' })
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
