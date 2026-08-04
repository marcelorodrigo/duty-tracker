<script setup lang="ts">
import type { CalendarFeedEvent, CalendarFeedPreview } from '~/types/calendarFeed'
import { formatDateTime } from '~/utils/dates'

defineProps<{
  preview: CalendarFeedPreview | null
  pending: boolean
  error: Error | null
  hasFeedUrl: boolean
  importEvent: (event: CalendarFeedEvent) => Promise<boolean>
}>()

const emit = defineEmits<{
  refresh: []
}>()
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-2">
        <UIcon
          name="i-lucide-calendar-days"
          class="text-(--ui-text-muted) size-4"
        />
        <h2 class="text-sm font-medium text-(--ui-text-muted)">
          Calendar feed
        </h2>
      </div>
      <UButton
        v-if="hasFeedUrl"
        variant="ghost"
        size="xs"
        icon="i-lucide-refresh-cw"
        :loading="pending"
        @click="emit('refresh')"
      >
        Refresh
      </UButton>
    </div>

    <div
      v-if="!hasFeedUrl"
      class="py-8 text-center space-y-3 border border-dashed border-(--ui-border) rounded-lg"
    >
      <UIcon
        name="i-lucide-link"
        class="text-3xl text-muted mx-auto"
      />
      <div class="space-y-1">
        <p class="text-sm font-medium">
          No calendar feed configured
        </p>
        <p class="text-xs text-muted">
          Add your incident.io ICS feed URL in settings to preview on-call events.
        </p>
      </div>
      <UButton
        to="/settings/profile"
        variant="outline"
        size="sm"
        icon="i-lucide-settings"
      >
        Configure feed
      </UButton>
    </div>

    <div
      v-else-if="pending && !preview"
      class="flex justify-center py-8"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-2xl text-muted"
      />
    </div>

    <div
      v-else-if="error"
      class="flex items-start gap-2 text-sm text-(--ui-color-error-500)"
    >
      <UIcon
        name="i-lucide-alert-triangle"
        class="size-4 shrink-0 mt-0.5"
      />
      <div class="space-y-0.5">
        <p>Failed to load calendar feed preview</p>
        <p class="text-xs text-(--ui-text-muted)">
          Please check the feed URL in your profile settings.
        </p>
      </div>
    </div>

    <div
      v-else-if="preview"
      class="space-y-6"
    >
      <CalendarFeedEventList
        v-if="preview.upcoming.length > 0"
        title="Upcoming"
        :events="preview.upcoming"
        :import-event="importEvent"
      />
      <CalendarFeedEventList
        v-if="preview.past.length > 0"
        title="Past"
        :events="preview.past"
        :import-event="importEvent"
      />
      <div
        v-if="preview.upcoming.length === 0 && preview.past.length === 0"
        class="py-8 text-center text-muted"
      >
        <p class="text-sm">
          No on-call events found in the feed.
        </p>
      </div>
    </div>
  </div>
</template>
