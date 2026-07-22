import { computed, reactive, shallowRef, toValue, watch } from 'vue'
import type { MaybeRefOrGetter } from 'vue'
import type { ProfileFormData } from '~/schemas/profile'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'
import { formatTime } from '~/utils/dates'
import { PROFILE_DAYS, type ProfileDay } from '~/utils/profile'

interface UseProfileFormOptions {
  profile: MaybeRefOrGetter<EngineerProfileResponse>
  save: (request: UpdateProfileRequest) => Promise<void>
}

export function useProfileForm({ profile, save }: UseProfileFormOptions) {
  const state = reactive<ProfileFormData>({
    workingDays: [],
    workStartTime: '',
    workEndTime: '',
    hourlyRate: null,
    standbyWeekdaySaturdayPercentage: null,
    standbyWeekdaySundayHolidayPercentage: null
  })
  const saving = shallowRef(false)
  const showRateWarning = shallowRef(false)
  const pendingRequest = shallowRef<UpdateProfileRequest | null>(null)

  watch(
    () => toValue(profile),
    (currentProfile) => {
      state.workingDays = PROFILE_DAYS.filter(day => currentProfile.workingDays.includes(day))
      state.workStartTime = formatTime(currentProfile.workStartTime)
      state.workEndTime = formatTime(currentProfile.workEndTime)
      state.hourlyRate = currentProfile.hourlyRate
      state.standbyWeekdaySaturdayPercentage = currentProfile.standbyWeekdaySaturdayPercentage
      state.standbyWeekdaySundayHolidayPercentage = currentProfile.standbyWeekdaySundayHolidayPercentage
    },
    { immediate: true }
  )

  const warningHourlyRate = computed(() => pendingRequest.value?.hourlyRate ?? state.hourlyRate)

  function toggleDay(day: ProfileDay): void {
    state.workingDays = state.workingDays.includes(day)
      ? state.workingDays.filter(currentDay => currentDay !== day)
      : [...state.workingDays, day]
  }

  function buildRequest(data: ProfileFormData): UpdateProfileRequest {
    return {
      workingDays: PROFILE_DAYS.filter(day => data.workingDays.includes(day)),
      workStartTime: `${data.workStartTime}:00`,
      workEndTime: `${data.workEndTime}:00`,
      hourlyRate: data.hourlyRate ?? undefined,
      standbyWeekdaySaturdayPercentage: data.standbyWeekdaySaturdayPercentage ?? undefined,
      standbyWeekdaySundayHolidayPercentage: data.standbyWeekdaySundayHolidayPercentage ?? undefined
    }
  }

  async function performSave(request: UpdateProfileRequest): Promise<void> {
    saving.value = true
    try {
      await save(request)
    } finally {
      saving.value = false
      showRateWarning.value = false
      pendingRequest.value = null
    }
  }

  async function submit(data: ProfileFormData): Promise<void> {
    const request = buildRequest(data)
    pendingRequest.value = request

    if (data.hourlyRate !== null && data.hourlyRate > 200) {
      showRateWarning.value = true
      return
    }

    await performSave(request)
  }

  async function confirmRateWarning(): Promise<void> {
    if (pendingRequest.value) {
      await performSave(pendingRequest.value)
    }
  }

  function dismissRateWarning(): void {
    showRateWarning.value = false
    pendingRequest.value = null
  }

  return {
    state,
    saving,
    showRateWarning,
    warningHourlyRate,
    toggleDay,
    submit,
    confirmRateWarning,
    dismissRateWarning
  }
}
