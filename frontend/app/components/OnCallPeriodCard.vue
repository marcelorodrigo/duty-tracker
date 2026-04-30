<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { formatDateTime } from '~/utils/dates'

defineProps<{
  period: OnCallPeriodResponse
  onEdit: (period: OnCallPeriodResponse) => void
  onDelete: (period: OnCallPeriodResponse) => void
}>()
</script>

<template>
  <div
    class="border border-(--ui-border) rounded-lg p-4 flex items-center justify-between hover:bg-(--ui-bg-elevated) transition-colors"
  >
    <div class="flex-1">
      <p class="text-sm font-medium">
        {{ formatDateTime(period.startDateTime) }} → {{ formatDateTime(period.endDateTime) }}
      </p>
      <p
        v-if="period.holidayDates.length > 0"
        class="text-xs text-(--ui-text-muted) mt-1"
      >
        {{ period.holidayDates.length }} {{ period.holidayDates.length === 1 ? 'holiday' : 'holidays' }}
      </p>
    </div>
    <div class="flex gap-2">
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
</template>
