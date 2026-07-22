<script setup lang="ts">
import type { AllowanceSavePayload, DayTypeCell } from '~/types/compensation'

const props = defineProps<{
  cell: DayTypeCell
}>()

const emit = defineEmits<{
  save: [payload: AllowanceSavePayload]
}>()

const editing = shallowRef(false)
const inputValue = shallowRef('')
const committed = shallowRef(false)

function clampInput() {
  const parsed = Number.parseFloat(inputValue.value)
  if (!Number.isNaN(parsed)) {
    inputValue.value = String(Math.min(100, Math.max(0, parsed)))
  }
}

function startEdit() {
  inputValue.value = String(props.cell.percentage)
  committed.value = false
  editing.value = true
}

function confirmEdit() {
  if (committed.value) return
  committed.value = true
  editing.value = false

  const parsed = Number.parseFloat(inputValue.value)
  if (Number.isNaN(parsed) || parsed < 0 || parsed > 100) return

  emit('save', { id: props.cell.id, percentage: parsed })
}

function cancelEdit() {
  if (committed.value) return
  committed.value = true
  editing.value = false
}
</script>

<template>
  <div class="min-w-16">
    <UInput
      v-if="editing"
      v-model="inputValue"
      type="number"
      min="0"
      max="100"
      size="xs"
      autofocus
      class="w-20"
      @input="clampInput"
      @keyup.enter="confirmEdit"
      @keydown.tab.prevent="confirmEdit"
      @keyup.escape="cancelEdit"
      @blur="cancelEdit"
    />
    <button
      v-else
      class="w-full text-left px-2 py-1 rounded hover:bg-elevated transition-colors cursor-pointer"
      @click="startEdit"
    >
      {{ cell.percentage }}%
    </button>
  </div>
</template>
