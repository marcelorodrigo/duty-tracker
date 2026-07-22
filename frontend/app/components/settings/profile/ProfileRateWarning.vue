<script setup lang="ts">
interface Props {
  open: boolean
  hourlyRate: number | null
  saving: boolean
}

interface Emits {
  cancel: []
  confirm: []
}

defineProps<Props>()
const emit = defineEmits<Emits>()

function onOpenChange(open: boolean): void {
  if (!open) {
    emit('cancel')
  }
}
</script>

<template>
  <UModal
    :open="open"
    @update:open="onOpenChange"
  >
    <template #title>
      High hourly rate
    </template>

    <template #body>
      <p class="text-sm text-(--ui-text-muted)">
        The hourly rate of <strong class="text-(--ui-text)">€{{ hourlyRate?.toFixed(2) }}</strong> is unusually high. Please confirm you want to continue.
      </p>
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton
          variant="ghost"
          color="neutral"
          @click="emit('cancel')"
        >
          Cancel
        </UButton>
        <UButton
          color="primary"
          :loading="saving"
          @click="emit('confirm')"
        >
          Confirm
        </UButton>
      </div>
    </template>
  </UModal>
</template>
