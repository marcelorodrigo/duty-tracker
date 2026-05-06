<script setup lang="ts">
import type { CalendarDate } from '@internationalized/date'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { calendarDateToISO, formatDateShort } from '~/utils/dates'

const props = defineProps<{
  mode: 'create' | 'edit'
  period?: OnCallPeriodResponse
}>()

const {
  dateRange,
  startTime,
  endTime,
  holidays,
  customHolidayDate,
  customHolidayName,
  customHolidayError,
  fetchingHolidays,
  saving,
  error,
  addCustomHoliday,
  removeHoliday,
  save,
} = useOnCallPeriodForm(props.mode, props.period)

const pageTitle = computed(() => (props.mode === 'create' ? 'New on-call period' : 'Edit on-call period'))

const rangeReady = computed(() => !!dateRange.value.start && !!dateRange.value.end)

const rangeLabel = computed(() => {
  if (!dateRange.value.start || !dateRange.value.end) return 'Select date range'
  return `${calendarDateToISO(dateRange.value.start as CalendarDate)} → ${calendarDateToISO(dateRange.value.end as CalendarDate)}`
})
</script>

<template>
  <UContainer>
    <div class="py-6 max-w-3xl mx-auto">
      <!-- Header -->
      <div class="flex items-center gap-2 mb-8">
        <NuxtLink to="/">
          <UButton
            icon="i-lucide-arrow-left"
            variant="ghost"
            color="neutral"
            aria-label="Back to periods"
          />
        </NuxtLink>
        <h1 class="text-2xl font-semibold">
          {{ pageTitle }}
        </h1>
      </div>

      <!-- Date Range -->
      <div class="mb-8">
        <h2 class="text-sm font-medium mb-3">
          Date range
        </h2>
        <p
          v-if="rangeReady"
          class="text-sm text-(--ui-text-muted) mb-3"
        >
          {{ rangeLabel }}
        </p>
        <div class="border border-(--ui-border) rounded-lg p-4 inline-block">
          <UCalendar
            v-model="dateRange"
            range
            :number-of-months="2"
            fixed-weeks
          />
        </div>
      </div>

      <!-- Time Inputs -->
      <div class="mb-8">
        <h2 class="text-sm font-medium mb-3">
          Time
        </h2>
        <div class="flex items-end gap-6">
          <UFormField
            label="Start time"
            class="flex-1"
          >
            <UInput
              v-model="startTime"
              type="time"
              class="w-full"
            />
          </UFormField>
          <UFormField
            label="End time"
            class="flex-1"
          >
            <UInput
              v-model="endTime"
              type="time"
              class="w-full"
            />
          </UFormField>
        </div>
      </div>

      <!-- Holidays -->
      <div
        v-if="rangeReady"
        class="mb-8"
      >
        <h2 class="text-sm font-medium mb-3">
          Holidays
        </h2>

        <!-- Loading suggestions -->
        <div
          v-if="fetchingHolidays"
          class="flex items-center gap-2 py-4 text-(--ui-text-muted)"
        >
          <UIcon
            name="i-lucide-loader-circle"
            class="animate-spin"
          />
          <span class="text-sm">Loading holiday suggestions…</span>
        </div>

        <!-- Holiday list -->
        <template v-else>
          <div
            v-if="holidays.length > 0"
            class="space-y-2 mb-4"
          >
            <div
              v-for="holiday in holidays"
              :key="holiday.date"
              class="flex items-center gap-3 border border-(--ui-border) rounded-lg p-3"
            >
              <span class="text-sm text-(--ui-text-muted) w-28 shrink-0">{{ formatDateShort(holiday.date) }}</span>
              <UInput
                v-model="holiday.name"
                placeholder="Holiday name (optional)"
                size="sm"
                class="flex-1"
              />
              <UButton
                icon="i-lucide-trash-2"
                variant="ghost"
                color="error"
                size="sm"
                :aria-label="`Remove holiday on ${holiday.date}`"
                @click="removeHoliday(holiday.date)"
              />
            </div>
          </div>

          <!-- Empty state -->
          <p
            v-else
            class="text-sm text-(--ui-text-muted) mb-4"
          >
            No holidays found for this period.
          </p>

          <!-- Add custom holiday -->
          <div class="border border-(--ui-border) rounded-lg p-4">
            <p class="text-sm font-medium mb-3">
              Add custom holiday
            </p>
            <div class="flex items-end gap-3">
              <UFormField
                label="Date"
                :error="customHolidayError ?? undefined"
                class="flex-1"
              >
                <UInputDate
                   v-model="customHolidayDate"
                   granularity="day"
                   :min-value="dateRange.start"
                   :max-value="dateRange.end"
                 />
              </UFormField>
              <UFormField
                label="Name (optional)"
                class="flex-1"
              >
                <UInput
                  v-model="customHolidayName"
                  placeholder="e.g. Company day off"
                />
              </UFormField>
              <UButton
                icon="i-lucide-plus"
                class="mb-0.5"
                @click="addCustomHoliday"
              >
                Add
              </UButton>
            </div>
          </div>
        </template>
      </div>

      <!-- Error -->
      <UAlert
        v-if="error"
        color="error"
        icon="i-lucide-alert-circle"
        :title="error"
        class="mb-6"
      />

      <!-- Action bar -->
      <div class="flex items-center justify-end gap-3">
        <NuxtLink to="/">
          <UButton
            variant="outline"
            color="neutral"
          >
            Cancel
          </UButton>
        </NuxtLink>
        <UButton
          :loading="saving"
          icon="i-lucide-check"
          @click="save"
        >
          Save
        </UButton>
      </div>
    </div>
  </UContainer>
</template>
