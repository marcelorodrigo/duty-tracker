<script setup lang="ts">
import { CalendarDateTime } from '@internationalized/date'
import type { OnCallPeriodResponse, CreateOnCallPeriodRequest, UpdateOnCallPeriodRequest } from '~/types/onCallPeriod'
import { nextMondayAt14, followingMondayAt14, toCalendarDateTime, fromCalendarDateTime } from '~/utils/dates'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  period: OnCallPeriodResponse | null
  onClose: () => void
  onSubmit: (request: CreateOnCallPeriodRequest | UpdateOnCallPeriodRequest) => Promise<void>
}>()

const startDateTime = ref<CalendarDateTime | null>(null)
const endDateTime = ref<CalendarDateTime | null>(null)
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
      startDateTime.value = toCalendarDateTime(start)
      endDateTime.value = toCalendarDateTime(end)
    } else if (props.mode === 'edit' && props.period) {
      startDateTime.value = toCalendarDateTime(new Date(props.period.startDateTime))
      endDateTime.value = toCalendarDateTime(new Date(props.period.endDateTime))
    }
  },
  { immediate: true }
)

function validate(): boolean {
  if (!startDateTime.value || !endDateTime.value) {
    validationError.value = 'Both start and end times are required.'
    return false
  }

  // Compare timestamps for proper date/time comparison
  const startTime = new Date(startDateTime.value.year, startDateTime.value.month - 1, startDateTime.value.day, startDateTime.value.hour, startDateTime.value.minute).getTime()
  const endTime = new Date(endDateTime.value.year, endDateTime.value.month - 1, endDateTime.value.day, endDateTime.value.hour, endDateTime.value.minute).getTime()

  if (startTime >= endTime) {
    validationError.value = 'End time must be after start time.'
    return false
  }

  validationError.value = ''
  return true
}

async function handleSubmit(): Promise<void> {
  if (!validate() || !startDateTime.value || !endDateTime.value) return

  submitting.value = true
  try {
    const request = {
      startDateTime: fromCalendarDateTime(startDateTime.value),
      endDateTime: fromCalendarDateTime(endDateTime.value)
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
          <UInputDate
            v-model="startDateTime"
            granularity="minute"
          />
        </UFormField>

        <UFormField
          label="End"
          required
        >
          <UInputDate
            v-model="endDateTime"
            granularity="minute"
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
