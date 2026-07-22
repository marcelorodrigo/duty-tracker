<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  hourlyRate: number | null
  standbyWeekdaySaturdayPercentage: number | null
  standbyWeekdaySundayHolidayPercentage: number | null
  submitAttempted: boolean
  rateError: string | null
  standbyWeekdaySaturdayError: string | null
  standbyWeekdaySundayHolidayError: string | null
}

interface Emits {
  'update:hourlyRate': [rate: number | null]
  'update:standbyWeekdaySaturdayPercentage': [percentage: number | null]
  'update:standbyWeekdaySundayHolidayPercentage': [percentage: number | null]
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const hourlyRate = computed({
  get: () => props.hourlyRate,
  set: value => emit('update:hourlyRate', value)
})

const weekdaySaturdayPercentage = computed({
  get: () => props.standbyWeekdaySaturdayPercentage,
  set: value => emit('update:standbyWeekdaySaturdayPercentage', value)
})

const sundayHolidayPercentage = computed({
  get: () => props.standbyWeekdaySundayHolidayPercentage,
  set: value => emit('update:standbyWeekdaySundayHolidayPercentage', value)
})
</script>

<template>
  <section class="space-y-5">
    <div>
      <h2 class="text-sm font-semibold text-(--ui-text)">
        Compensation
      </h2>
      <p class="mt-0.5 text-xs text-muted">
        Used to estimate the cost of on-call periods and incidents.
      </p>
    </div>

    <USeparator />

    <div class="space-y-1.5">
      <label
        class="block text-sm font-medium"
        for="hourly-rate"
      >Hourly rate</label>
      <div class="flex max-w-48 items-center gap-2">
        <span class="select-none text-sm text-muted">€</span>
        <UInput
          id="hourly-rate"
          v-model.number="hourlyRate"
          type="number"
          step="0.01"
          min="0"
          placeholder="0.00"
          class="flex-1"
          :error="props.submitAttempted && !!props.rateError"
        />
      </div>
      <p
        v-if="props.submitAttempted && props.rateError"
        class="text-xs text-(--ui-color-error-500)"
      >
        {{ props.rateError }}
      </p>
    </div>
  </section>

  <section class="space-y-5">
    <div>
      <h2 class="text-sm font-semibold text-(--ui-text)">
        Standby Hours Compensation
      </h2>
      <p class="mt-0.5 text-xs text-muted">
        Percentage of monthly salary (hourly rate × 160 h) paid per standby day type.
      </p>
    </div>

    <USeparator />

    <div class="space-y-1.5">
      <label
        class="block text-sm font-medium"
        for="standby-weekday-saturday"
      >Weekday / Saturday</label>
      <UInput
        id="standby-weekday-saturday"
        v-model.number="weekdaySaturdayPercentage"
        type="number"
        step="0.001"
        min="0.001"
        placeholder="0.067"
        class="max-w-48"
        :error="props.submitAttempted && !!props.standbyWeekdaySaturdayError"
      />
      <p
        v-if="props.submitAttempted && props.standbyWeekdaySaturdayError"
        class="text-xs text-(--ui-color-error-500)"
      >
        {{ props.standbyWeekdaySaturdayError }}
      </p>
    </div>

    <div class="space-y-1.5">
      <label
        class="block text-sm font-medium"
        for="standby-sunday-holiday"
      >Sunday / Holiday</label>
      <UInput
        id="standby-sunday-holiday"
        v-model.number="sundayHolidayPercentage"
        type="number"
        step="0.001"
        min="0.001"
        placeholder="0.084"
        class="max-w-48"
        :error="props.submitAttempted && !!props.standbyWeekdaySundayHolidayError"
      />
      <p
        v-if="props.submitAttempted && props.standbyWeekdaySundayHolidayError"
        class="text-xs text-(--ui-color-error-500)"
      >
        {{ props.standbyWeekdaySundayHolidayError }}
      </p>
    </div>
  </section>
</template>
