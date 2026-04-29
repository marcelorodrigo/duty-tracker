import { defineStore } from 'pinia'

export interface CompensationRate {
  id: number
  employeeType: 'INTERNAL' | 'EXTERNAL'
  rateCategory: 'ONCALL_WEEKDAY_SATURDAY' | 'ONCALL_SUNDAY_HOLIDAY' | 'OVERTIME_BASE' | 'OVERTIME_ALLOWANCE'
  overtimeDayType: 'WEEKDAY' | 'SATURDAY' | 'SUNDAY_HOLIDAY' | null
  label: string
  timeFrom: string | null
  timeTo: string | null
  percentage: string
}

export const useCompensationStore = defineStore('compensation', () => {
  const rates = ref<CompensationRate[]>([])
  const api = useApi()
  const toast = useToast()

  async function fetchRates(employeeType?: 'INTERNAL' | 'EXTERNAL') {
    const path = employeeType ? `/compensation-rates?employeeType=${employeeType}` : '/compensation-rates'
    const res = await api.get<{ rates: CompensationRate[] }>(path)
    rates.value = res.rates
    return res.rates
  }

  async function createRate(data: Omit<CompensationRate, 'id'>) {
    const rate = await api.post<CompensationRate>('/compensation-rates', data)
    rates.value.push(rate)
    return rate
  }

  async function updateRate(id: number, data: { percentage: string; label: string }) {
    const rate = await api.put<CompensationRate>(`/compensation-rates/${id}`, data)
    const idx = rates.value.findIndex(r => r.id === id)
    if (idx >= 0) rates.value[idx] = rate
    toast.add({ title: 'Rate updated', color: 'success', icon: 'i-lucide-check' })
    return rate
  }

  async function deleteRate(id: number) {
    await api.del(`/compensation-rates/${id}`)
    rates.value = rates.value.filter(r => r.id !== id)
  }

  return { rates, fetchRates, createRate, updateRate, deleteRate }
})
