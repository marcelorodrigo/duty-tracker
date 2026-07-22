<script setup lang="ts">
import { reactive } from 'vue'
import type { FormSubmitEvent } from '@nuxt/ui'
import type { IncidentResponse, CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { DateValue } from '@internationalized/date'
import { parseDateTime, now, getLocalTimeZone } from '@internationalized/date'
import {
  createIncidentFormSchema,
  isIncidentEndAfterPeriod,
  toIncidentDateTimeString,
  type IncidentFormData
} from '~/schemas/incident'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  incident: IncidentResponse | null
  onCallPeriodId: number
  onCallPeriod: OnCallPeriodResponse
  submitting: boolean
}>()

const emit = defineEmits<{
  close: []
  submit: [request: CreateIncidentRequest | UpdateIncidentRequest]
}>()

const name = shallowRef('')
const startDateTime = shallowRef<DateValue>()
const endDateTime = shallowRef<DateValue>()
const formState = reactive({ name, startDateTime, endDateTime })
const formSchema = computed(() => createIncidentFormSchema(props.onCallPeriod))
const pendingWarningSubmission = shallowRef<IncidentFormData>()
const showEndDateWarning = shallowRef(false)

watch(() => props.open, (isOpen) => {
  if (!isOpen) return
  pendingWarningSubmission.value = undefined
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

function submitIncident(data: IncidentFormData): void {
  const startStr = toIncidentDateTimeString(data.startDateTime)
  const endStr = toIncidentDateTimeString(data.endDateTime)

  if (props.mode === 'create') {
    emit('submit', {
      onCallPeriodId: props.onCallPeriodId,
      name: data.name,
      startDateTime: startStr,
      endDateTime: endStr
    } satisfies CreateIncidentRequest)
  } else {
    emit('submit', {
      name: data.name,
      startDateTime: startStr,
      endDateTime: endStr
    } satisfies UpdateIncidentRequest)
  }
}

function handleSubmit(event: FormSubmitEvent<IncidentFormData>): void {
  if (isIncidentEndAfterPeriod(event.data.endDateTime, props.onCallPeriod)) {
    pendingWarningSubmission.value = event.data
    showEndDateWarning.value = true
    return
  }

  submitIncident(event.data)
}

function handleWarningConfirm(): void {
  const submission = pendingWarningSubmission.value
  if (!submission) return

  pendingWarningSubmission.value = undefined
  showEndDateWarning.value = false
  submitIncident(submission)
}

function handleWarningCancel() {
  pendingWarningSubmission.value = undefined
  showEndDateWarning.value = false
}
</script>

<template>
  <UModal
    :open="open"
    @update:open="(val: boolean) => { if (!val) emit('close') }"
  >
    <template #title>
      {{ mode === 'create' ? 'Log incident' : 'Edit incident' }}
    </template>

    <template #body>
      <UForm
        id="incident-form"
        :schema="formSchema"
        :state="formState"
        class="space-y-4"
        @submit="handleSubmit"
      >
        <UFormField
          name="name"
          label="Name"
          required
        >
          <UInput
            id="incident-name"
            v-model="formState.name"
            placeholder="e.g. Database failover"
            class="w-full"
          />
        </UFormField>

        <UFormField
          name="startDateTime"
          label="Start date/time"
          required
        >
          <UInputDate
            id="incident-start-date-time"
            v-model="formState.startDateTime"
            granularity="minute"
            class="w-full"
          />
        </UFormField>

        <UFormField
          name="endDateTime"
          label="End date/time"
          required
        >
          <UInputDate
            id="incident-end-date-time"
            v-model="formState.endDateTime"
            granularity="minute"
            class="w-full"
          />
        </UFormField>
      </UForm>
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton
          variant="ghost"
          color="neutral"
          @click="emit('close')"
        >
          Cancel
        </UButton>
        <UButton
          type="submit"
          form="incident-form"
          :loading="submitting"
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
          :loading="submitting"
          @click="handleWarningConfirm"
        >
          Continue anyway
        </UButton>
      </div>
    </template>
  </UModal>
</template>
