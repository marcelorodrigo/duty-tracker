const API_VERSION_PREFIX = '/api/v1'
const API_TIMEOUT_MS = 10_000
const API_ACCEPT_HEADER = 'application/json, application/problem+json'

export type ApiEndpoint = `/${string}`
export type ApiQuery = Record<string, string | number | boolean | null | undefined>
export type ApiMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  [extension: string]: unknown
}

export interface ApiTransportOptions {
  method: ApiMethod
  headers: Record<string, string>
  query?: ApiQuery
  body?: unknown
  timeout: number
  retry: number
}

export interface ApiTransport {
  <TResponse>(request: string, options: ApiTransportOptions): Promise<TResponse>
}

export interface ApiRequestOptions {
  query?: ApiQuery
}

export interface ApiClient {
  get<TResponse>(path: ApiEndpoint, options?: ApiRequestOptions): Promise<TResponse>
  post<TResponse, TBody = unknown>(path: ApiEndpoint, body: TBody, options?: ApiRequestOptions): Promise<TResponse>
  put<TResponse, TBody = unknown>(path: ApiEndpoint, body: TBody, options?: ApiRequestOptions): Promise<TResponse>
  delete<TResponse = void>(path: ApiEndpoint, options?: ApiRequestOptions): Promise<TResponse>
}

export class ApiProblem extends Error {
  readonly type?: string
  readonly title?: string
  readonly status?: number
  readonly detail?: string
  readonly instance?: string
  readonly problem: Readonly<ProblemDetail>

  constructor(problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? 'API request failed')
    this.name = 'ApiProblem'
    this.type = problem.type
    this.title = problem.title
    this.status = problem.status
    this.detail = problem.detail
    this.instance = problem.instance
    this.problem = Object.freeze({ ...problem })
  }
}

export function apiPath(path: ApiEndpoint): string {
  return `${API_VERSION_PREFIX}${path}`
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function stringValue(value: unknown): string | undefined {
  return typeof value === 'string' ? value : undefined
}

function statusValue(value: unknown): number | undefined {
  return typeof value === 'number' && Number.isInteger(value) ? value : undefined
}

function problemFromError(error: unknown): ProblemDetail | null {
  if (!isRecord(error) || !isRecord(error.data)) return null

  const data = error.data
  const bodyStatus = statusValue(data.status)
  const status = bodyStatus
    ?? statusValue(error.statusCode)
    ?? statusValue(error.status)
    ?? (isRecord(error.response) ? statusValue(error.response.status) : undefined)
  const type = stringValue(data.type)
  const title = stringValue(data.title)
  const detail = stringValue(data.detail)
  const instance = stringValue(data.instance)

  if (
    type === undefined
    && title === undefined
    && detail === undefined
    && instance === undefined
    && bodyStatus === undefined
  ) return null

  const problem: ProblemDetail = {}
  for (const [key, value] of Object.entries(data)) {
    if (!['type', 'title', 'status', 'detail', 'instance'].includes(key)) {
      problem[key] = value
    }
  }

  return {
    ...problem,
    ...(type !== undefined ? { type } : {}),
    ...(title !== undefined ? { title } : {}),
    ...(status !== undefined ? { status } : {}),
    ...(detail !== undefined ? { detail } : {}),
    ...(instance !== undefined ? { instance } : {})
  }
}

export function createApiClient(transport: ApiTransport): ApiClient {
  async function request<TResponse>(
    method: ApiMethod,
    path: ApiEndpoint,
    options: ApiRequestOptions = {},
    body?: unknown
  ): Promise<TResponse> {
    try {
      return await transport<TResponse>(apiPath(path), {
        method,
        headers: { accept: API_ACCEPT_HEADER },
        timeout: API_TIMEOUT_MS,
        retry: 0,
        ...options,
        ...(body === undefined ? {} : { body })
      })
    } catch (error) {
      const problem = problemFromError(error)
      if (problem) throw new ApiProblem(problem)
      throw error
    }
  }

  return {
    get: (path, options) => request('GET', path, options),
    post: (path, body, options) => request('POST', path, options, body),
    put: (path, body, options) => request('PUT', path, options, body),
    delete: (path, options) => request('DELETE', path, options)
  }
}
