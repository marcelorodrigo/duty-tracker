<template>
  <div>
    <div class="flex flex-wrap gap-2 mb-3">
      <span v-if="period.holidayOverrides.length === 0" class="text-gray-400 text-sm">No holiday overrides</span>
      <UBadge
        v-for="date in period.holidayOverrides"
        :key="date"
        color="warning"
        class="flex items-center gap-1"
      >
        {{ date }}
        <UButton size="xs" variant="ghost" icon="i-heroicons-x-mark" @click="removeHoliday(date)" />
      </UBadge>
    </div>
    <UPopover>
      <UButton size="sm" variant="outline">+ Add Holiday</UButton>
      <template #content>
        <div class="p-3 flex flex-col gap-2">
          <UInput type="date" v-model="newDate" />
          <UButton size="sm" :disabled="!newDate" @click="addHoliday">Add</UButton>
        </div>
      </template>
    </UPopover>
    <UAlert v-if="error" color="error" :description="error" class="mt-2" />
  </div>
</template>

<script setup lang="ts">
import type { OnCallPeriod } from '~/stores/oncall'

const props = defineProps<{ period: OnCallPeriod }>()
const oncallStore = useOnCallStore()
const newDate = ref('')
const error = ref<string | null>(null)

async function addHoliday() {
  if (!newDate.value) return
  error.value = null
  try {
    await oncallStore.addHolidayOverride(props.period.id, newDate.value)
    newDate.value = ''
  } catch {
    error.value = 'Failed to add holiday override.'
  }
}

async function removeHoliday(date: string) {
  error.value = null
  try {
    await oncallStore.removeHolidayOverride(props.period.id, date)
  } catch {
    error.value = 'Failed to remove holiday override.'
  }
}
</script>
