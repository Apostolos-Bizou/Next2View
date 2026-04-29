let eventSource = null
let reconnectTimer = null
const listeners = []

export function connectSSE() {
  if (eventSource) return

  const token = sessionStorage.getItem('access_token')
  if (!token) return

  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  const url = baseURL + '/sse/subscribe?token=' + encodeURIComponent(token)

  eventSource = new EventSource(url)

  eventSource.addEventListener('connected', (e) => {
    console.log('[SSE] Connected')
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  })

  eventSource.addEventListener('activity', (e) => {
    try {
      const data = JSON.parse(e.data)
      listeners.forEach(fn => fn(data))
    } catch (err) {
      console.error('[SSE] Parse error:', err)
    }
  })

  eventSource.onerror = () => {
    console.warn('[SSE] Connection lost, reconnecting in 5s...')
    disconnectSSE()
    reconnectTimer = setTimeout(() => connectSSE(), 5000)
  }
}

export function disconnectSSE() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

export function onActivity(callback) {
  listeners.push(callback)
  return () => {
    const idx = listeners.indexOf(callback)
    if (idx >= 0) listeners.splice(idx, 1)
  }
}
