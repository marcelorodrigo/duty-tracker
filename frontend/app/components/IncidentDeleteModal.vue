<script setup lang="ts">
import type { IncidentResponse } from '~/types/incident'
import { formatDate, formatTime } from '~/utils/dates'

defineProps<{
  open: boolean
  incident: IncidentResponse | null
  onClose: () => void
  onConfirm: () => Promise<void>
}>()

const deleting = ref(false)

async function handleConfirm(onConfirm: () => Promise<void>) {
  deleting.value = true
  try {
    await onConfirm()
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <UModal
    :open="open"
    @update:open="(val: boolean) => { if (!val) onClose() }"
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
        ({{ formatDate(incident.date) }}, {{ formatTime(incident.startTime) }}–{{ formatTime(incident.endTime) }})?
        This action cannot be undone.
      </p>
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton
          variant="ghost"
          color="neutral"
          @click="onClose"
        >
          Cancel
        </UButton>
        <UButton
          color="error"
          :loading="deleting"
          @click="handleConfirm(onConfirm)"
        >
          Delete
        </UButton>
      </div>
    </template>
  </UModal>
</template>
