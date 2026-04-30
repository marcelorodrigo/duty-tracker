<script setup lang="ts">
import type { OnCallPeriodResponse, CreateOnCallPeriodRequest, UpdateOnCallPeriodRequest } from '~/types/onCallPeriod'
import { nextMondayAt14, followingMondayAt14, toDatetimeLocal, fromDatetimeLocal } from '~/utils/dates'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  period: OnCallPeriodResponse | null
  onClose: () => void
  onSubmit: (request: CreateOnCallPeriodRequest | UpdateOnCallPeriodRequest) => Promise<void>
}>()

const startDateTime = ref('')
const endDateTime = ref('')
const submitting = ref(false)
const validationError = ref('')

const isOpen = computed({
  get: () => props.open,
  set: (val: boolean) => {
    if (!val) props.onClose()
  }
})

watch(
  () => [props.open, props.mode, props.period],
  () => {
    if (!props.open) return
    validationError.value = ''

    if (props.mode === 'create') {
      const start = nextMondayAt14()
      const end = followingMondayAt14(start)
      startDateTime.value = toDatetimeLocal(start)
      endDateTime.value = toDatetimeLocal(end)
    } else if (props.mode === 'edit' && props.period) {
      startDateTime.value = props.period.startDateTime.slice(0, 16)
      endDateTime.value = props.period.endDateTime.slice(0, 16)
    }
  },
  { immediate: true }
)

function validate(): boolean {
  if (!startDateTime.value || !endDateTime.value) {
    validationError.value = 'Both start and end times are required.'
    return false
  }

  if (startDateTime.value >= endDateTime.value) {
    validationError.value = 'End time must be after start time.'
    return false
  }

  validationError.value = ''
  return true
}

async function handleSubmit(): Promise<void> {
  if (!validate()) return

  submitting.value = true
  try {
    const request = {
      startDateTime: fromDatetimeLocal(startDateTime.value),
      endDateTime: fromDatetimeLocal(endDateTime.value)
    }
    await props.onSubmit(request)
  } finally {
    submitting.value = false
  }
}

const title = computed(() => (props.mode === 'create' ? 'New on-call period' : 'Edit on-call period'))
</script>

<template>
  <UModal v-model:open="isOpen">
    <template #title>
      {{ title }}
    </template>

    <template #body>
      <form
        v-if="props.open"
        class="space-y-4"
        @submit.prevent="handleSubmit"
      >
        <UFormField
          label="Start"
          required
        >
          <UInput
            v-model="startDateTime"
            type="datetime-local"
          />
        </UFormField>

        <UFormField
          label="End"
          required
        >
          <UInput
            v-model="endDateTime"
            type="datetime-local"
          />
        </UFormField>

        <UAlert
          v-if="validationError"
          color="error"
          icon="i-lucide-alert-circle"
          :title="validationError"
          class="mb-4"
        />
      </form>
    </template>

    <template #footer>
      <div
        v-if="props.open"
        class="flex justify-end gap-2"
      >
        <UButton
          variant="ghost"
          :disabled="submitting"
          @click="props.onClose"
        >
          Cancel
        </UButton>
        <UButton
          :loading="submitting"
          @click="handleSubmit"
        >
          Save
        </UButton>
      </div>
    </template>
  </UModal>
</template>
