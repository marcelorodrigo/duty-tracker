<template>
  <div class="inline-flex items-center min-w-[4rem]">
    <template v-if="editing">
      <UInput
        ref="inputRef"
        v-model="inputValue"
        type="number"
        step="0.01"
        min="0"
        size="xs"
        class="w-20"
        :loading="saving"
        @blur="commitEdit"
        @keydown.enter.prevent="handleEnter"
        @keydown.escape="cancelEdit"
      />
    </template>
    <template v-else>
      <button
        type="button"
        class="cursor-pointer rounded px-1.5 py-0.5 hover:bg-gray-100 dark:hover:bg-gray-800 text-sm tabular-nums select-none"
        aria-label="Edit percentage"
        @click="startEdit"
      >
        {{ displayValue }}%
      </button>
    </template>
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue: string
  rateId: number
  onSave: (id: number, percentage: string) => Promise<void>
}

const props = defineProps<Props>()

const editing = ref(false)
const saving = ref(false)
const inputValue = ref('')
const inputRef = ref<{ $el: HTMLInputElement } | null>(null)

const displayValue = computed(() => {
  const n = parseFloat(props.modelValue)
  return isNaN(n) ? '0' : n.toString()
})

async function startEdit() {
  inputValue.value = displayValue.value
  editing.value = true
  await nextTick()
  inputRef.value?.$el?.querySelector('input')?.focus()
}

function handleEnter() {
  // Blur triggers commitEdit — one code path, one API call
  inputRef.value?.$el?.querySelector('input')?.blur()
}

async function commitEdit() {
  const raw = inputValue.value
  const trimmed = (raw == null ? '' : String(raw)).trim()
  if (!trimmed || isNaN(Number(trimmed)) || Number(trimmed) < 0) {
    cancelEdit()
    return
  }
  saving.value = true
  try {
    await props.onSave(props.rateId, trimmed)
    editing.value = false
  } finally {
    saving.value = false
  }
}

function cancelEdit() {
  editing.value = false
  inputValue.value = ''
}
</script>
