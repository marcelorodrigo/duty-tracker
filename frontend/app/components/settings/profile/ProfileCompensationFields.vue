<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  hourlyRate: number | null
  standbyWeekdaySaturdayPercentage: number | null
  standbyWeekdaySundayHolidayPercentage: number | null
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

    <UFormField
      name="hourlyRate"
      label="Hourly rate"
      description="Gross compensation per incident hour, in euros."
    >
      <div class="flex max-w-48 items-center gap-2">
        <span
          aria-hidden="true"
          class="select-none text-sm text-muted"
        >€</span>
        <UInput
          id="hourly-rate"
          v-model.number="hourlyRate"
          type="number"
          step="0.01"
          min="0"
          placeholder="0.00"
          class="flex-1"
        />
      </div>
    </UFormField>
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

    <UFormField
      name="standbyWeekdaySaturdayPercentage"
      label="Weekday / Saturday"
      description="Salary fraction paid for a weekday or Saturday on standby."
    >
      <UInput
        id="standby-weekday-saturday"
        v-model.number="weekdaySaturdayPercentage"
        type="number"
        step="0.001"
        min="0.001"
        placeholder="0.067"
        class="max-w-48"
      />
    </UFormField>

    <UFormField
      name="standbyWeekdaySundayHolidayPercentage"
      label="Sunday / Holiday"
      description="Salary fraction paid for a Sunday or holiday on standby."
    >
      <UInput
        id="standby-sunday-holiday"
        v-model.number="sundayHolidayPercentage"
        type="number"
        step="0.001"
        min="0.001"
        placeholder="0.084"
        class="max-w-48"
      />
    </UFormField>
  </section>
</template>
