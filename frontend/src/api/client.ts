export class ApiError extends Error {
  readonly status: number
  readonly code?: string

  constructor(message: string, status: number, code?: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

async function parseProblem(response: Response): Promise<ApiError> {
  try {
    const body = await response.json() as { detail?: string; title?: string; code?: string }
    return new ApiError(body.detail || body.title || response.statusText, response.status, body.code)
  } catch {
    return new ApiError(response.statusText || 'Request failed', response.status)
  }
}

export type ApiOptions = {
  method?: string
  body?: unknown
  signal?: AbortSignal
}

export async function apiRequest<T = void>(path: string, options: ApiOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(path, {
    method: options.method ?? (options.body !== undefined ? 'POST' : 'GET'),
    headers,
    credentials: 'include',
    signal: options.signal,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })

  if (!response.ok) {
    throw await parseProblem(response)
  }

  if (response.status === 204 || response.status === 202) {
    return undefined as T
  }

  const text = await response.text()
  if (!text) {
    return undefined as T
  }
  return JSON.parse(text) as T
}
