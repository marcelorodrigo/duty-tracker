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

function percentageBgColor(pct: number): string {
  const bracket = Math.min(10, Math.floor(pct / 10))
  // opacity steps: 0, 5, 10, 15, 20, 30, 40, 50, 60, 75, 90 (out of 100)
  const opacities = [0, 5, 10, 15, 20, 30, 40, 50, 60, 75, 90]
  return `color-mix(in srgb, var(--ui-primary) ${opacities[bracket]!}%, transparent)`
}

const displayBgColor = computed(() => percentageBgColor(props.cell.percentage))

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
      @blur="confirmEdit"
    />
    <button
      v-else
      class="w-full text-left px-2 py-1 rounded hover:bg-elevated transition-colors cursor-pointer"
      :class="{ 'opacity-50': saving }"
      :style="{ backgroundColor: displayBgColor }"
      @click="startEdit"
    >
      {{ cell.percentage }}%
    </button>
  </div>
</template>
