<script setup lang="ts">
import type { IncidentResponse } from '~/types/incident'
import { formatDateTime } from '~/utils/dates'

defineProps<{
  open: boolean
  incident: IncidentResponse | null
  deleting: boolean
}>()

const emit = defineEmits<{
  close: []
  confirm: []
}>()
</script>

<template>
  <UModal
    :open="open"
    @update:open="(val: boolean) => { if (!val) emit('close') }"
  >
    <template #title>
      Delete incident
    </template>

    <template #body>
      <p
        v-if="incident"
        class="text-sm text-(--ui-text-muted)"
      >
        Are you sure you want to delete <strong class="text-(--ui-text)">{{ incident.name }}</strong>
        ({{ formatDateTime(incident.startDateTime) }}–{{ formatDateTime(incident.endDateTime) }})?
        This action cannot be undone.
      </p>
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton
          variant="ghost"
          color="neutral"
          @click="emit('close')"
        >
          Cancel
        </UButton>
        <UButton
          color="error"
          :loading="deleting"
          @click="emit('confirm')"
        >
          Delete
        </UButton>
      </div>
    </template>
  </UModal>
</template>
