<template>
  <UForm :schema="schema" :state="form" @submit="onSubmit">
    <UFormField label="Start Date & Time" name="startDateTime">
      <UInput type="datetime-local" v-model="form.startDateTime" />
    </UFormField>
    <UFormField label="End Date & Time" name="endDateTime">
      <UInput type="datetime-local" v-model="form.endDateTime" />
    </UFormField>
    <UAlert v-if="error" color="error" :description="error" class="mt-2" />
    <div class="mt-4">
      <UButton type="submit" :loading="loading">Create Period</UButton>
    </div>
  </UForm>
</template>

<script setup lang="ts">
import { z } from 'zod'

const props = defineProps<{
  onSubmitAsync?: (data: { startDateTime: string; endDateTime: string }) => Promise<void>
}>()

const emit = defineEmits<{ submit: [data: { startDateTime: string; endDateTime: string }] }>()

const loading = ref(false)
const error = ref<string | null>(null)

const schema = z.object({
  startDateTime: z.string().min(1, 'Start date is required'),
  endDateTime: z.string().min(1, 'End date is required'),
}).refine(data => new Date(data.endDateTime) > new Date(data.startDateTime), {
  message: 'End must be after start',
  path: ['endDateTime'],
})

const form = reactive({ startDateTime: '', endDateTime: '' })

async function onSubmit() {
  loading.value = true
  error.value = null
  try {
    const data = { startDateTime: form.startDateTime, endDateTime: form.endDateTime }
    if (props.onSubmitAsync) {
      await props.onSubmitAsync(data)
    } else {
      emit('submit', data)
    }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to create period.'
  } finally {
    loading.value = false
  }
}
</script>
