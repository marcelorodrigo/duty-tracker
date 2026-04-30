<script setup lang="ts">
import type { Ref } from 'vue'
import type { EmployeeType } from '~/types/compensation'

const employeeType = inject<Ref<EmployeeType | null>>('employeeType', ref(null))
const { pivotRows, pending, error, updateRate } = useCompensationRates(employeeType)
</script>

<template>
  <div>
    <p class="text-sm text-(--ui-text-muted) mb-4">
      Overtime allowance percentages applied to your hours.
      <template v-if="employeeType">
        Employee type: <strong>{{ employeeType }}</strong>.
      </template>
    </p>

    <div v-if="pending" class="flex justify-center py-12">
      <UIcon name="i-lucide-loader-circle" class="animate-spin text-2xl text-(--ui-text-muted)" />
    </div>

    <UAlert
      v-else-if="error"
      color="error"
      icon="i-lucide-alert-circle"
      title="Failed to load allowance rates"
      description="Please reload the page to try again."
    />

    <SettingsAllowanceTable
      v-else-if="pivotRows.length > 0"
      :rows="pivotRows"
      :on-save="updateRate"
    />
  </div>
</template>
