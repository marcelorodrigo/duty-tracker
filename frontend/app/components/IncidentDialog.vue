<script setup lang="ts">
import type { IncidentResponse, CreateIncidentRequest, UpdateIncidentRequest } from '~/types/incident'
import { CalendarDate } from '@internationalized/date'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  incident: IncidentResponse | null
  onCallPeriodId: number
  onClose: () => void
  onSubmit: (request: CreateIncidentRequest | UpdateIncidentRequest) => Promise<void>
}>()

const saving = ref(false)
const name = ref('')
const date = ref<CalendarDate | undefined>()
const startTime = ref('')
const endTime = ref('')

const validationError = ref('')

watch(() => props.open, (isOpen) => {
  if (!isOpen) return
  validationError.value = ''

  if (props.mode === 'edit' && props.incident) {
    name.value = props.incident.name
    const [year, month, day] = props.incident.date.split('-').map(Number)
    date.value = new CalendarDate(year!, month!, day!)
    startTime.value = props.incident.startTime.substring(0, 5)
    endTime.value = props.incident.endTime.substring(0, 5)
  } else {
    name.value = ''
    const today = new Date()
    date.value = new CalendarDate(today.getFullYear(), today.getMonth() + 1, today.getDate())
    startTime.value = ''
    endTime.value = ''
  }
})

function formatCalendarDate(d: CalendarDate): string {
  const year = d.year
  const month = String(d.month).padStart(2, '0')
  const day = String(d.day).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function handleSubmit() {
  if (!name.value.trim()) {
    validationError.value = 'Name is required.'
    return
  }
  if (!date.value) {
    validationError.value = 'Date is required.'
    return
  }
  if (!startTime.value || !endTime.value) {
    validationError.value = 'Start time and end time are required.'
    return
  }

  validationError.value = ''
  saving.value = true
  try {
    const dateStr = formatCalendarDate(date.value)
    const startTimeStr = `${startTime.value}:00`
    const endTimeStr = `${endTime.value}:00`

    if (props.mode === 'create') {
      await props.onSubmit({
        onCallPeriodId: props.onCallPeriodId,
        name: name.value.trim(),
        date: dateStr,
        startTime: startTimeStr,
        endTime: endTimeStr
      } as CreateIncidentRequest)
    } else {
      await props.onSubmit({
        name: name.value.trim(),
        date: dateStr,
        startTime: startTimeStr,
        endTime: endTimeStr
      } as UpdateIncidentRequest)
    }
  } finally {
    saving.value = false
  }
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
          label="Date"
          required
        >
          <UPopover>
            <UButton
              type="button"
              variant="outline"
              color="neutral"
              :label="date ? formatCalendarDate(date) : 'Select date'"
              icon="i-lucide-calendar"
              class="w-full justify-start"
            />

            <template #content>
              <UCalendar v-model="date" />
            </template>
          </UPopover>
        </UFormField>

        <div class="grid grid-cols-2 gap-4">
          <UFormField
            label="Start time"
            required
          >
            <UInput
              v-model="startTime"
              type="time"
              class="w-full"
            />
          </UFormField>

          <UFormField
            label="End time"
            required
          >
            <UInput
              v-model="endTime"
              type="time"
              class="w-full"
            />
          </UFormField>
        </div>

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
</template>
