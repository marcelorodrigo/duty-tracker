<script setup lang="ts">
import type { DayTypeCell } from '~/types/compensation'

const props = defineProps<{
  cell: DayTypeCell
  onSave: (id: number, percentage: number) => Promise<void>
}>()

const editing = ref(false)
const inputValue = ref('')
const saving = ref(false)
const committed = ref(false)

function clampInput() {
  const parsed = parseFloat(inputValue.value)
  if (!isNaN(parsed)) {
    inputValue.value = String(Math.min(100, Math.max(0, parsed)))
  }
}

function startEdit() {
  inputValue.value = String(props.cell.percentage)
  committed.value = false
  editing.value = true
}

async function confirmEdit() {
  if (committed.value) return
  committed.value = true
  editing.value = false

  const parsed = parseFloat(inputValue.value)
  if (isNaN(parsed) || parsed < 0 || parsed > 100) return

  saving.value = true
  await props.onSave(props.cell.id, parsed)
  saving.value = false
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
      :loading="saving"
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
      :class="{ 'opacity-50': saving }"
      @click="startEdit"
    >
      {{ cell.percentage }}%
    </button>
  </div>
</template>
