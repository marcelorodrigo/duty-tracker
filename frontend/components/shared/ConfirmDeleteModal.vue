<template>
  <UModal :open="open" @update:open="emit('update:open', $event)" title="Confirm Delete">
    <template #body>
      <div class="p-4 space-y-4">
        <p>Are you sure you want to delete <strong>{{ itemLabel }}</strong>?</p>
        <p class="text-sm text-gray-500">This action cannot be undone.</p>
        <div class="flex gap-2 justify-end">
          <UButton color="neutral" variant="outline" @click="emit('update:open', false)">Cancel</UButton>
          <UButton color="error" @click="onConfirm">Delete</UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
defineProps<{ open: boolean; itemLabel: string }>()
const emit = defineEmits<{
  'update:open': [value: boolean]
  confirmed: []
}>()

function onConfirm() {
  emit('confirmed')
  emit('update:open', false)
}
</script>
