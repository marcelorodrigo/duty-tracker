<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { formatDateTime, getPeriodStatus, getStatusColors } from '~/utils/dates'

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

        <!-- Stats row -->
        <div class="flex items-center gap-3 mt-1.5 text-xs text-(--ui-text-muted)">
          <span
            v-if="period.holidays.length > 0"
            class="inline-flex items-center gap-1"
          >
            <UIcon
              name="i-lucide-palm-tree"
              class="text-sm"
            />
            {{ period.holidays.length }} {{ period.holidays.length === 1 ? 'holiday' : 'holidays' }}
          </span>
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
