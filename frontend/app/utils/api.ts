const API_VERSION_PREFIX = '/api/v1'

export function apiPath(path: `/${string}`): string {
  return `${API_VERSION_PREFIX}${path}`
}
