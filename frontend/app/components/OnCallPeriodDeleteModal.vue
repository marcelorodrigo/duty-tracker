<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import { formatDate } from '~/utils/dates'

const props = defineProps<{
  open: boolean
  period: OnCallPeriodResponse | null
  onClose: () => void
  onConfirm: () => Promise<void>
}>()

const confirming = ref(false)

const isOpen = computed({
  get: () => props.open,
  set: (val: boolean) => {
    if (!val) props.onClose()
  }
})

async function handleConfirm(): Promise<void> {
  confirming.value = true
  try {
    await props.onConfirm()
  } finally {
    confirming.value = false
  }
}

const dateRange = computed(() => {
  if (!props.period) return ''
  return `${formatDate(props.period.startDateTime)} to ${formatDate(props.period.endDateTime)}`
})
</script>

<template>
  <UModal v-model:open="isOpen">
    <template #title>
      Delete on-call period
    </template>

    <div
      v-if="props.open"
      class="space-y-4"
    >
      <p class="text-sm text-(--ui-text)">
        This will permanently delete the on-call period from <strong>{{ dateRange }}</strong>. This action cannot be undone.
      </p>
    </div>

    <template #footer>
      <div
        v-if="props.open"
        class="flex justify-end gap-2"
      >
        <UButton
          variant="ghost"
          :disabled="confirming"
          @click="props.onClose"
        >
          Cancel
        </UButton>
        <UButton
          color="error"
          :loading="confirming"
          @click="handleConfirm"
        >
          Delete
        </UButton>
      </div>
    </template>
  </UModal>
</template>
