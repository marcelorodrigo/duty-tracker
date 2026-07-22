import { readonly, shallowRef } from 'vue'
import type { AllowanceSavePayload, AllowanceSaveState } from '~/types/compensation'

interface UseAllowanceRateSavesOptions {
  save: (id: number, percentage: number) => Promise<boolean>
}

const rejectedMessage = 'Could not save. Your edit is still available; review it and try again.'

export function useAllowanceRateSaves({ save }: UseAllowanceRateSavesOptions) {
  const saveStates = shallowRef<Record<number, AllowanceSaveState>>({})

  function setSaveState(id: number, state: AllowanceSaveState): void {
    saveStates.value = { ...saveStates.value, [id]: state }
  }

  async function saveRate(payload: AllowanceSavePayload): Promise<void> {
    if (saveStates.value[payload.id]?.status === 'saving') return

    setSaveState(payload.id, { status: 'saving' })
    const saved = await save(payload.id, payload.percentage).catch(() => false)
    setSaveState(payload.id, saved
      ? { status: 'idle' }
      : { status: 'rejected', message: rejectedMessage })
  }

  return {
    saveStates: readonly(saveStates),
    saveRate
  }
}
