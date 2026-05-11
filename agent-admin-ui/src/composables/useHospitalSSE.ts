import { ref, onMounted, onUnmounted } from 'vue'

export interface HospitalStatus {
  id: number
  orgCode: string
  orgName: string
  baseUrl: string
  apiKey: string
  status: number
  online: boolean
  lastHeartbeatTime: number | null
}

export function useHospitalSSE() {
  const hospitals = ref<HospitalStatus[]>([])
  const connected = ref(false)
  let eventSource: EventSource | null = null
  let reconnectTimer: number | null = null

  function connect() {
    if (eventSource) eventSource.close()
    eventSource = new EventSource('/agent/hospital/status-stream')

    eventSource.addEventListener('init', (e) => {
      const msg = JSON.parse(e.data)
      if (msg.data) {
        hospitals.value = msg.data.map((h: any) => ({
          ...h,
          online: h.online === true
        }))
      }
      connected.value = true
    })

    eventSource.addEventListener('status', (e) => {
      const msg = JSON.parse(e.data)
      const idx = hospitals.value.findIndex(h => h.orgCode === msg.orgCode)
      if (idx !== -1) {
        hospitals.value[idx].online = msg.online === true
        hospitals.value[idx].lastHeartbeatTime = msg.lastHeartbeatTime
      }
    })

    eventSource.onerror = () => {
      connected.value = false
      eventSource?.close()
      eventSource = null
      if (reconnectTimer) clearTimeout(reconnectTimer)
      reconnectTimer = window.setTimeout(connect, 3000)
    }
  }

  onMounted(() => connect())

  onUnmounted(() => {
    eventSource?.close()
    if (reconnectTimer) clearTimeout(reconnectTimer)
  })

  return { hospitals, connected }
}
