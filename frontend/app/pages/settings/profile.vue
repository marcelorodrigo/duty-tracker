<script setup lang="ts">
import type { UpdateProfileRequest } from '~/types/profile'
import { formatTime } from '~/utils/dates'

const { profile, pending, error, save } = useProfile()

const DAYS_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'] as const
const DAY_LABELS: Record<string, string> = {
  MONDAY: 'Mon',
  TUESDAY: 'Tue',
  WEDNESDAY: 'Wed',
  THURSDAY: 'Thu',
  FRIDAY: 'Fri',
  SATURDAY: 'Sat',
  SUNDAY: 'Sun'
}

// Form state — initialised from profile once loaded
const workingDays = ref<string[]>([])
const workStartTime = ref('')
const workEndTime = ref('')
const hourlyRate = ref<number | null>(null)
const standbyWeekdaySaturdayPercentage = ref<number | null>(null)
const standbyWeekdaySundayHolidayPercentage = ref<number | null>(null)
const saving = ref(false)
const showRateWarning = ref(false)
const submitAttempted = ref(false)

// Sync form state when profile loads
watch(profile, (p) => {
  if (!p) return
  workingDays.value = [...p.workingDays]
  workStartTime.value = formatTime(p.workStartTime)
  workEndTime.value = formatTime(p.workEndTime)
  hourlyRate.value = p.hourlyRate
  standbyWeekdaySaturdayPercentage.value = p.standbyWeekdaySaturdayPercentage
  standbyWeekdaySundayHolidayPercentage.value = p.standbyWeekdaySundayHolidayPercentage
}, { immediate: true })

function toggleDay(day: string) {
  const idx = workingDays.value.indexOf(day)
  if (idx >= 0) {
    workingDays.value = workingDays.value.filter(d => d !== day)
  } else {
    workingDays.value = [...workingDays.value, day]
  }
}

const rateError = computed(() => {
  if (hourlyRate.value === null || hourlyRate.value === undefined) {
    return null
  }
  if (hourlyRate.value <= 1) {
    return 'Hourly rate must be greater than 1.00'
  }
  return null
})

const rateWarning = computed(() => {
  return hourlyRate.value !== null && hourlyRate.value > 200
})

const standbyWeekdaySaturdayError = computed(() => {
  if (standbyWeekdaySaturdayPercentage.value === null || standbyWeekdaySaturdayPercentage.value === undefined) {
    return null
  }
  if (standbyWeekdaySaturdayPercentage.value < 0.001) {
    return 'Weekday / Saturday percentage must be at least 0.001'
  }
  return null
})

const standbyWeekdaySundayHolidayError = computed(() => {
  if (standbyWeekdaySundayHolidayPercentage.value === null || standbyWeekdaySundayHolidayPercentage.value === undefined) {
    return null
  }
  if (standbyWeekdaySundayHolidayPercentage.value < 0.001) {
    return 'Sunday / Holiday percentage must be at least 0.001'
  }
  return null
})

async function onSubmit() {
  submitAttempted.value = true

  if (rateError.value || standbyWeekdaySaturdayError.value || standbyWeekdaySundayHolidayError.value) {
    return
  }

  if (rateWarning.value) {
    showRateWarning.value = true
    return
  }

  await performSave()
}

async function performSave() {
  saving.value = true
  const request: UpdateProfileRequest = {
    workingDays: DAYS_ORDER.filter(d => workingDays.value.includes(d)),
    workStartTime: workStartTime.value + ':00',
    workEndTime: workEndTime.value + ':00',
    hourlyRate: hourlyRate.value ?? undefined,
    standbyWeekdaySaturdayPercentage: standbyWeekdaySaturdayPercentage.value ?? undefined,
    standbyWeekdaySundayHolidayPercentage: standbyWeekdaySundayHolidayPercentage.value ?? undefined
  }
  await save(request)
  saving.value = false
  showRateWarning.value = false
}
</script>

<template>
  <div class="max-w-2xl">
    <div
      v-if="pending"
      class="flex justify-center py-12"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-2xl text-muted"
      />
    </div>

    <UAlert
      v-else-if="error"
      color="error"
      icon="i-lucide-alert-circle"
      title="Failed to load profile"
      description="Please reload the page to try again."
    />

    <div
      v-else-if="!profile"
      class="py-12 text-center text-muted"
    >
      <p class="text-sm">
        No profile found.
      </p>
    </div>

    <form
      v-else
      class="space-y-8"
      @submit.prevent="onSubmit"
    >
      <!-- Work schedule section -->
      <div class="space-y-5">
        <div>
          <h2 class="text-sm font-semibold text-(--ui-text)">
            Work schedule
          </h2>
          <p class="text-xs text-muted mt-0.5">
            Your typical working days and hours, used to calculate on-call effort.
          </p>
        </div>

        <USeparator />

        <!-- Working days -->
        <div class="space-y-2">
          <label class="block text-sm font-medium">Working days</label>
          <div class="flex gap-2 flex-wrap">
            <button
              v-for="day in DAYS_ORDER"
              :key="day"
              type="button"
              class="px-3 py-1.5 rounded-md border text-sm font-medium transition-colors"
              :class="workingDays.includes(day)
                ? 'border-(--ui-color-primary-500) text-(--ui-color-primary-500) bg-(--ui-color-primary-50) dark:bg-(--ui-color-primary-950)'
                : 'border-default text-muted hover:text-default hover:border-accented'"
              @click="toggleDay(day)"
            >
              {{ DAY_LABELS[day] }}
            </button>
          </div>
        </div>

        <!-- Work hours -->
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-1.5">
            <label
              class="block text-sm font-medium"
              for="work-start-time"
            >Start time</label>
            <UInput
              id="work-start-time"
              v-model="workStartTime"
              type="time"
            />
          </div>
          <div class="space-y-1.5">
            <label
              class="block text-sm font-medium"
              for="work-end-time"
            >End time</label>
            <UInput
              id="work-end-time"
              v-model="workEndTime"
              type="time"
            />
          </div>
        </div>
      </div>

      <!-- Compensation section -->
      <div class="space-y-5">
        <div>
          <h2 class="text-sm font-semibold text-(--ui-text)">
            Compensation
          </h2>
          <p class="text-xs text-muted mt-0.5">
            Used to estimate the cost of on-call periods and incidents.
          </p>
        </div>

        <USeparator />

        <!-- Hourly rate -->
        <div class="space-y-1.5">
          <label
            class="block text-sm font-medium"
            for="hourly-rate"
          >Hourly rate</label>
          <div class="flex items-center gap-2 max-w-48">
            <span class="text-sm text-muted select-none">€</span>
            <UInput
              id="hourly-rate"
              v-model.number="hourlyRate"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
              class="flex-1"
              :error="submitAttempted && !!rateError"
            />
          </div>
          <p
            v-if="submitAttempted && rateError"
            class="text-(--ui-color-error-500) text-xs"
          >
            {{ rateError }}
          </p>
        </div>
      </div>

      <!-- Standby Hours Compensation section -->
      <div class="space-y-5">
        <div>
          <h2 class="text-sm font-semibold text-(--ui-text)">
            Standby Hours Compensation
          </h2>
          <p class="text-xs text-muted mt-0.5">
            Percentage of monthly salary (hourly rate × 160 h) paid per standby day type.
          </p>
        </div>

        <USeparator />

        <!-- Weekday / Saturday -->
        <div class="space-y-1.5">
          <label
            class="block text-sm font-medium"
            for="standby-weekday-saturday"
          >Weekday / Saturday</label>
          <UInput
            id="standby-weekday-saturday"
            v-model.number="standbyWeekdaySaturdayPercentage"
            type="number"
            step="0.001"
            min="0.001"
            placeholder="0.067"
            class="max-w-48"
            :error="submitAttempted && !!standbyWeekdaySaturdayError"
          />
          <p
            v-if="submitAttempted && standbyWeekdaySaturdayError"
            class="text-(--ui-color-error-500) text-xs"
          >
            {{ standbyWeekdaySaturdayError }}
          </p>
        </div>

        <!-- Sunday / Holiday -->
        <div class="space-y-1.5">
          <label
            class="block text-sm font-medium"
            for="standby-sunday-holiday"
          >Sunday / Holiday</label>
          <UInput
            id="standby-sunday-holiday"
            v-model.number="standbyWeekdaySundayHolidayPercentage"
            type="number"
            step="0.001"
            min="0.001"
            placeholder="0.084"
            class="max-w-48"
            :error="submitAttempted && !!standbyWeekdaySundayHolidayError"
          />
          <p
            v-if="submitAttempted && standbyWeekdaySundayHolidayError"
            class="text-(--ui-color-error-500) text-xs"
          >
            {{ standbyWeekdaySundayHolidayError }}
          </p>
        </div>
      </div>

      <!-- Actions -->
      <div class="pt-2">
        <UButton
          type="submit"
          :loading="saving"
          :disabled="saving"
          icon="i-lucide-save"
        >
          Save profile
        </UButton>
      </div>
    </form>

    <!-- Rate warning modal -->
    <UModal
      :open="showRateWarning"
      @update:open="(val: boolean) => { if (!val) showRateWarning = false }"
    >
      <template #title>
        High hourly rate
      </template>

      <template #body>
        <p class="text-sm text-(--ui-text-muted)">
          The hourly rate of <strong class="text-(--ui-text)">€{{ hourlyRate?.toFixed(2) }}</strong> is unusually high. Please confirm you want to continue.
        </p>
      </template>

      <template #footer>
        <div class="flex justify-end gap-2">
          <UButton
            variant="ghost"
            color="neutral"
            @click="showRateWarning = false"
          >
            Cancel
          </UButton>
          <UButton
            color="primary"
            :loading="saving"
            @click="performSave"
          >
            Confirm
          </UButton>
        </div>
      </template>
    </UModal>
  </div>
</template>
