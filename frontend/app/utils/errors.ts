/**
 * extractErrorDetail
 * Extracts a human-readable error message from an ofetch error response.
 * Looks for the RFC 9457 Problem Detail `detail` field first,
 * then falls back to `message`, then a default string.
 */
export function extractErrorDetail(err: unknown, fallback = 'Please try again.'): string {
  if (err && typeof err === 'object') {
    if ('detail' in err && typeof err.detail === 'string') return err.detail

    const data = 'data' in err
      ? (err as { data?: { detail?: string, message?: string } }).data
      : undefined
    if (data?.detail) return data.detail
    if (data?.message) return data.message
  }
  return fallback
}
