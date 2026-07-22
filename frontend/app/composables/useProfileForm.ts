import { computed, ref, shallowRef, toValue, watch } from 'vue'
import type { MaybeRefOrGetter } from 'vue'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'
import { formatTime } from '~/utils/dates'
import { PROFILE_DAYS, type ProfileDay } from '~/utils/profile'

interface UseProfileFormOptions {
  profile: MaybeRefOrGetter<EngineerProfileResponse>
  save: (request: UpdateProfileRequest) => Promise<void>
}

export function useProfileForm({ profile, save }: UseProfileFormOptions) {
  const workingDays = ref<string[]>([])
  const workStartTime = shallowRef('')
  const workEndTime = shallowRef('')
  const hourlyRate = shallowRef<number | null>(null)
  const standbyWeekdaySaturdayPercentage = shallowRef<number | null>(null)
  const standbyWeekdaySundayHolidayPercentage = shallowRef<number | null>(null)
  const saving = shallowRef(false)
  const showRateWarning = shallowRef(false)
  const submitAttempted = shallowRef(false)

  watch(
    () => toValue(profile),
    (currentProfile) => {
      workingDays.value = [...currentProfile.workingDays]
      workStartTime.value = formatTime(currentProfile.workStartTime)
      workEndTime.value = formatTime(currentProfile.workEndTime)
      hourlyRate.value = currentProfile.hourlyRate
      standbyWeekdaySaturdayPercentage.value = currentProfile.standbyWeekdaySaturdayPercentage
      standbyWeekdaySundayHolidayPercentage.value = currentProfile.standbyWeekdaySundayHolidayPercentage
    },
    { immediate: true }
  )

  const rateError = computed(() => {
    if (hourlyRate.value === null || hourlyRate.value === undefined) {
      return null
    }
    return hourlyRate.value <= 1
      ? 'Hourly rate must be greater than 1.00'
      : null
  })

  const standbyWeekdaySaturdayError = computed(() => {
    if (
      standbyWeekdaySaturdayPercentage.value === null
      || standbyWeekdaySaturdayPercentage.value === undefined
    ) {
      return null
    }
    return standbyWeekdaySaturdayPercentage.value < 0.001
      ? 'Weekday / Saturday percentage must be at least 0.001'
      : null
  })

  const standbyWeekdaySundayHolidayError = computed(() => {
    if (
      standbyWeekdaySundayHolidayPercentage.value === null
      || standbyWeekdaySundayHolidayPercentage.value === undefined
    ) {
      return null
    }
    return standbyWeekdaySundayHolidayPercentage.value < 0.001
      ? 'Sunday / Holiday percentage must be at least 0.001'
      : null
  })

  const hasValidationError = computed(() => Boolean(
    rateError.value
    || standbyWeekdaySaturdayError.value
    || standbyWeekdaySundayHolidayError.value
  ))

  const hasRateWarning = computed(() => hourlyRate.value !== null && hourlyRate.value > 200)

  function toggleDay(day: ProfileDay): void {
    workingDays.value = workingDays.value.includes(day)
      ? workingDays.value.filter(currentDay => currentDay !== day)
      : [...workingDays.value, day]
  }

  function buildRequest(): UpdateProfileRequest {
    return {
      workingDays: PROFILE_DAYS.filter(day => workingDays.value.includes(day)),
      workStartTime: `${workStartTime.value}:00`,
      workEndTime: `${workEndTime.value}:00`,
      hourlyRate: hourlyRate.value ?? undefined,
      standbyWeekdaySaturdayPercentage: standbyWeekdaySaturdayPercentage.value ?? undefined,
      standbyWeekdaySundayHolidayPercentage: standbyWeekdaySundayHolidayPercentage.value ?? undefined
    }
  }

  async function performSave(): Promise<void> {
    saving.value = true
    try {
      await save(buildRequest())
    } finally {
      saving.value = false
      showRateWarning.value = false
    }
  }

  async function submit(): Promise<void> {
    submitAttempted.value = true

    if (hasValidationError.value) {
      return
    }

    if (hasRateWarning.value) {
      showRateWarning.value = true
      return
    }

    await performSave()
  }

  function dismissRateWarning(): void {
    showRateWarning.value = false
  }

  return {
    workingDays,
    workStartTime,
    workEndTime,
    hourlyRate,
    standbyWeekdaySaturdayPercentage,
    standbyWeekdaySundayHolidayPercentage,
    saving,
    showRateWarning,
    submitAttempted,
    rateError,
    standbyWeekdaySaturdayError,
    standbyWeekdaySundayHolidayError,
    toggleDay,
    submit,
    confirmRateWarning: performSave,
    dismissRateWarning
  }
}
