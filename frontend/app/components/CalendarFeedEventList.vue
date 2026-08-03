<script setup lang="ts">
import { ref } from 'vue'
import type { CalendarFeedEvent } from '~/types/calendarFeed'
import { formatDateTime } from '~/utils/dates'

const props = defineProps<{
  title: string
  events: CalendarFeedEvent[]
  importEvent: (event: CalendarFeedEvent) => Promise<boolean>
}>()

const importingEventKey = ref<string | null>(null)

function eventKey(event: CalendarFeedEvent): string {
  return event.startDateTime + event.endDateTime + event.summary
}

function isImporting(event: CalendarFeedEvent): boolean {
  return importingEventKey.value === eventKey(event)
}

async function handleImport(event: CalendarFeedEvent) {
  const key = eventKey(event)
  importingEventKey.value = key
  try {
    await props.importEvent(event)
  } catch {
    // importEvent already handles and surfaces errors via toast
  } finally {
    importingEventKey.value = null
  }
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
          :loading="isImporting(event)"
          :disabled="isImporting(event)"
          @click="handleImport(event)"
        >
          Import
        </UButton>
      </li>
    </ul>
  </div>
</template>
