<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { formatDateTime, formatDateShort, getPeriodStatus, getStatusColors } from '~/utils/dates'

const props = defineProps<{
  period: OnCallPeriodResponse
  onEdit: (period: OnCallPeriodResponse) => void
  onDelete: (period: OnCallPeriodResponse) => void
}>()

const status = computed(() => getPeriodStatus(props.period.startDateTime, props.period.endDateTime))

const statusText = computed(() => {
  switch (status.value) {
    case 'scheduled':
      return 'Scheduled'
    case 'active':
      return 'Active'
    case 'past':
      return 'Past'
  }
})

const colors = computed(() => getStatusColors(status.value))
</script>

<template>
  <NuxtLink
    :to="`/oncall/${period.id}`"
    class="block border border-(--ui-border) rounded-lg p-4 hover:border-(--ui-border-accented) hover:bg-(--ui-bg-elevated) transition-colors cursor-pointer"
  >
    <div class="flex items-start justify-between">
      <div class="flex-1 min-w-0">
        <!-- Status pill -->
        <span
          class="inline-flex items-center gap-1.5 text-xs font-medium px-2 py-0.5 rounded-full mb-2"
          :class="colors.badge"
        >
          <span
            class="size-1.5 rounded-full"
            :class="colors.dot"
          />
          {{ statusText }}
        </span>

        <!-- Date range -->
        <p class="text-sm font-medium">
          {{ formatDateTime(period.startDateTime) }} → {{ formatDateTime(period.endDateTime) }}
        </p>

        <!-- Holidays section -->
        <div
          v-if="period.holidays.length > 0"
          class="mt-3 pl-3 border-l-2 border-(--ui-border) py-2"
        >
          <div class="flex items-center gap-2 mb-2">
            <UIcon
              name="i-lucide-calendar-days"
              class="size-3.5 text-(--ui-text-muted) flex-shrink-0"
            />
            <span class="text-xs font-medium text-(--ui-text-muted)">Holidays</span>
          </div>
          <ul class="space-y-1">
            <li
              v-for="holiday in period.holidays"
              :key="holiday.date"
              class="text-xs text-(--ui-text-muted) flex items-start gap-2"
            >
              <span class="flex-shrink-0 mt-0.5">−</span>
              <span>{{ holiday.name ?? 'Holiday' }} • {{ formatDateShort(holiday.date) }}</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- Actions -->
      <div
        class="flex gap-1 shrink-0 ml-4"
        @click.prevent
      >
        <UButton
          aria-label="Edit period"
          icon="i-lucide-pencil"
          variant="ghost"
          size="sm"
          color="neutral"
          @click="onEdit(period)"
        />
        <UButton
          aria-label="Delete period"
          icon="i-lucide-trash-2"
          variant="ghost"
          size="sm"
          color="error"
          @click="onDelete(period)"
        />
      </div>
    </div>
  </NuxtLink>
</template>
