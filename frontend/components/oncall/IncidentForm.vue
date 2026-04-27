<template>
  <UForm :schema="schema" :state="form" @submit="onSubmit">
    <UFormField label="Date" name="date">
      <UInput type="date" v-model="form.date" />
    </UFormField>
    <UFormField label="Start Time" name="startTime">
      <UInput type="time" v-model="form.startTime" />
    </UFormField>
    <UFormField label="End Time" name="endTime">
      <UInput type="time" v-model="form.endTime" />
    </UFormField>
    <UAlert v-if="error" color="error" :description="error" class="mt-2" />
    <UAlert
      v-if="dayOffWarning"
      color="warning"
      title="Day Off"
      description="This day is flagged as a day off. Overtime pay does not apply — time-for-time applies instead."
      class="mt-2"
    />
    <div class="mt-4">
      <UButton type="submit" :loading="loading">Add Incident</UButton>
    </div>
  </UForm>
</template>

<script setup lang="ts">
import { z } from 'zod'

const props = defineProps<{
  periodId: number
  onSubmitAsync: (data: { onCallPeriodId: number; date: string; startTime: string; endTime: string }) => Promise<void>
}>()

const oncallStore = useOnCallStore()
const loading = ref(false)
const error = ref<string | null>(null)

const dayOffWarning = computed(() =>
  !!form.date && oncallStore.dayEntries.some(e => e.date === form.date && e.timeForTimeFlag)
)

const schema = z.object({
  date: z.string().min(1, 'Date is required'),
  startTime: z.string().min(1, 'Start time is required'),
  endTime: z.string().min(1, 'End time is required'),
})

const form = reactive({ date: '', startTime: '', endTime: '' })

async function onSubmit() {
  loading.value = true
  error.value = null
  try {
    await props.onSubmitAsync({ onCallPeriodId: props.periodId, ...form })
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to add incident.'
  } finally {
    loading.value = false
  }
}
</script>
