<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { FormErrorEvent, FormSubmitEvent } from '@nuxt/ui'
import {
  allowanceRateSchema,
  type AllowanceRateFormData
} from '~/schemas/allowance'
import type { AllowanceSavePayload, AllowanceSaveState, DayTypeCell } from '~/types/compensation'

type EditPhase = 'display' | 'editing' | 'validating' | 'saving'

interface AllowanceFormRef {
  submit: () => Promise<void>
}

const props = defineProps<{
  cell: DayTypeCell
  saveState: AllowanceSaveState
}>()

const emit = defineEmits<{
  save: [payload: AllowanceSavePayload]
}>()

const phase = shallowRef<EditPhase>('display')
const serverError = shallowRef<string>()
const form = useTemplateRef<AllowanceFormRef>('form')
const formState = reactive({
  percentage: String(props.cell.percentage)
})

const isEditing = computed(() => phase.value !== 'display')
const isSaving = computed(() => phase.value === 'saving')

function startEdit(): void {
  formState.percentage = String(props.cell.percentage)
  serverError.value = undefined
  phase.value = 'editing'
}

function submitDraft(): void {
  if (phase.value !== 'editing') return
  serverError.value = undefined
  phase.value = 'validating'
  void form.value?.submit()
}

function submitEdit(event: FormSubmitEvent<AllowanceRateFormData>): void {
  if (phase.value !== 'validating') return
  phase.value = 'saving'
  serverError.value = undefined
  emit('save', { id: props.cell.id, percentage: event.data.percentage })
}

function restoreInvalidDraft(_event: FormErrorEvent): void {
  if (phase.value === 'validating') phase.value = 'editing'
}

function cancelEdit(): void {
  if (phase.value !== 'editing') return
  serverError.value = undefined
  phase.value = 'display'
}

watch(
  () => props.saveState,
  (saveState) => {
    if (phase.value !== 'saving') return

    if (saveState.status === 'idle') {
      phase.value = 'display'
    } else if (saveState.status === 'rejected') {
      serverError.value = saveState.message
      phase.value = 'editing'
    }
  }
)
</script>

<template>
  <div class="min-w-16">
    <UForm
      v-if="isEditing"
      ref="form"
      :schema="allowanceRateSchema"
      :state="formState"
      :loading-auto="false"
      @submit="submitEdit"
      @error="restoreInvalidDraft"
    >
      <UFormField
        name="percentage"
        :error="serverError"
      >
        <UInput
          v-model="formState.percentage"
          type="number"
          size="xs"
          autofocus
          :disabled="isSaving"
          :loading="isSaving"
          class="w-20"
          aria-label="Allowance percentage"
          @keydown.enter.prevent="submitDraft"
          @keydown.tab="submitDraft"
          @keyup.escape.prevent="cancelEdit"
          @blur="cancelEdit"
        />
      </UFormField>
    </UForm>
    <button
      v-else
      type="button"
      class="w-full text-left px-2 py-1 rounded hover:bg-elevated transition-colors cursor-pointer"
      @click="startEdit"
    >
      {{ cell.percentage }}%
    </button>
  </div>
</template>
