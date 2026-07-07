<script setup lang="ts">
import type { IncidentResponse, CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { DateValue } from '@internationalized/date'
import { parseDateTime, now, getLocalTimeZone } from '@internationalized/date'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  incident: IncidentResponse | null
  onCallPeriodId: number
  onCallPeriod: OnCallPeriodResponse
  onClose: () => void
  onSubmit: (request: CreateIncidentRequest | UpdateIncidentRequest) => Promise<void>
}>()

const saving = ref(false)
const name = ref('')
const startDateTime = ref<DateValue | undefined>()
const endDateTime = ref<DateValue | undefined>()

const validationError = ref('')
const showEndDateWarning = ref(false)

watch(() => props.open, (isOpen) => {
  if (!isOpen) return
  validationError.value = ''
  showEndDateWarning.value = false

  if (props.mode === 'edit' && props.incident) {
    name.value = props.incident.name
    startDateTime.value = parseDateTime(props.incident.startDateTime.substring(0, 16))
    endDateTime.value = parseDateTime(props.incident.endDateTime.substring(0, 16))
  } else {
    name.value = ''
    // Set default datetime to current time for create mode
    const currentDateTime = now(getLocalTimeZone())
    startDateTime.value = currentDateTime
    endDateTime.value = currentDateTime
  }
})

function toIsoString(dt: DateValue): string {
  const year = dt.year
  const month = String(dt.month).padStart(2, '0')
  const day = String(dt.day).padStart(2, '0')
  // DateValue with granularity=minute has hour/minute via casting
  const withTime = dt as DateValue & { hour?: number, minute?: number }
  const hour = String(withTime.hour ?? 0).padStart(2, '0')
  const minute = String(withTime.minute ?? 0).padStart(2, '0')
  return `${year}-${month}-${day}T${hour}:${minute}:00`
}

function isBeforePeriod(isoStr: string): boolean {
  return new Date(isoStr) < new Date(props.onCallPeriod.startDateTime)
}

function isAfterPeriodEnd(isoStr: string): boolean {
  return new Date(isoStr) > new Date(props.onCallPeriod.endDateTime)
}

async function doSubmit() {
  const startStr = toIsoString(startDateTime.value!)
  const endStr = toIsoString(endDateTime.value!)

  if (props.mode === 'create') {
    await props.onSubmit({
      onCallPeriodId: props.onCallPeriodId,
      name: name.value.trim(),
      startDateTime: startStr,
      endDateTime: endStr
    } as CreateIncidentRequest)
  } else {
    await props.onSubmit({
      name: name.value.trim(),
      startDateTime: startStr,
      endDateTime: endStr
    } as UpdateIncidentRequest)
  }
}

async function handleSubmit() {
  if (!name.value.trim()) {
    validationError.value = 'Name is required.'
    return
  }
  if (!startDateTime.value) {
    validationError.value = 'Start date/time is required.'
    return
  }
  if (!endDateTime.value) {
    validationError.value = 'End date/time is required.'
    return
  }

  const startStr = toIsoString(startDateTime.value)
  const endStr = toIsoString(endDateTime.value)

  if (isBeforePeriod(startStr) || isAfterPeriodEnd(startStr)) {
    validationError.value = 'Start date/time must be within the on-call period window.'
    return
  }

  validationError.value = ''

  if (isAfterPeriodEnd(endStr)) {
    showEndDateWarning.value = true
    return
  }

  saving.value = true
  try {
    await doSubmit()
  } finally {
    saving.value = false
  }
}

async function handleWarningConfirm() {
  showEndDateWarning.value = false
  saving.value = true
  try {
    await doSubmit()
  } finally {
    saving.value = false
  }
}

function handleWarningCancel() {
  showEndDateWarning.value = false
}
</script>

<template>
  <UModal
    :open="open"
    @update:open="(val: boolean) => { if (!val) onClose() }"
  >
    <template #title>
      {{ mode === 'create' ? 'Log incident' : 'Edit incident' }}
    </template>

    <template #body>
      <form
        class="space-y-4"
        @submit.prevent="handleSubmit"
      >
        <UFormField
          label="Name"
          required
        >
          <UInput
            v-model="name"
            placeholder="e.g. Database failover"
            class="w-full"
          />
        </UFormField>

        <UFormField
          label="Start date/time"
          required
        >
          <UInputDate
            v-model="startDateTime"
            granularity="minute"
            class="w-full"
          />
        </UFormField>

        <UFormField
          label="End date/time"
          required
        >
          <UInputDate
            v-model="endDateTime"
            granularity="minute"
            class="w-full"
          />
        </UFormField>

        <p
          v-if="validationError"
          class="text-sm text-(--ui-color-error-500)"
        >
          {{ validationError }}
        </p>
      </form>
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
          :loading="saving"
          @click="handleSubmit"
        >
          {{ mode === 'create' ? 'Log incident' : 'Save changes' }}
        </UButton>
      </div>
    </template>
  </UModal>

  <!-- End date/time outside period warning -->
  <UModal
    :open="showEndDateWarning"
    @update:open="(val: boolean) => { if (!val) handleWarningCancel() }"
  >
    <template #title>
      End time outside period
    </template>

    <template #body>
      <p class="text-sm">
        The end date/time is outside the on-call period window. Are you sure you want to continue?
      </p>
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton
          variant="ghost"
          color="neutral"
          @click="handleWarningCancel"
        >
          Cancel
        </UButton>
        <UButton
          color="warning"
          :loading="saving"
          @click="handleWarningConfirm"
        >
          Continue anyway
        </UButton>
      </div>
    </template>
  </UModal>
</template>
