<script setup lang="ts">
import type { CalendarFeedEvent } from '~/types/calendarFeed'
import { formatDateTime } from '~/utils/dates'

const props = defineProps<{
  title: string
  events: CalendarFeedEvent[]
  importEvent: (event: CalendarFeedEvent) => Promise<boolean>
  importing: boolean
}>()

function handleImport(event: CalendarFeedEvent) {
  // importEvent surfaces errors via toast; the loading state is driven by the
  // mutation's pending status through the `importing` prop.
  props.importEvent(event)
}
</script>

<template>
  <div>
    <h3 class="text-xs font-medium text-(--ui-text-muted) mb-2">
      {{ title }}
    </h3>
    <ul class="space-y-2">
      <li
        v-for="event in events"
        :key="event.startDateTime + event.endDateTime + event.summary"
        class="flex items-center justify-between p-3 rounded-lg bg-(--ui-bg-elevated) border border-(--ui-border)"
      >
        <div class="min-w-0">
          <p class="text-sm font-medium truncate">
            {{ event.summary }}
          </p>
          <p class="text-xs text-muted">
            {{ formatDateTime(event.startDateTime) }} - {{ formatDateTime(event.endDateTime) }}
          </p>
        </div>
        <UButton
          variant="ghost"
          size="xs"
          icon="i-lucide-download"
          :loading="importing"
          :disabled="importing"
          @click="handleImport(event)"
        >
          Import
        </UButton>
      </li>
    </ul>
  </div>
</template>
