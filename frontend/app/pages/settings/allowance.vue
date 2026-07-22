<script setup lang="ts">
const { pivotRows, pending, error, refresh, updateRate } = useCompensationRates()
const errorMessage = computed(() => error.value ? getApiErrorMessage(error.value) : '')

onMounted(refresh)
</script>

<template>
  <div>
    <p class="text-sm text-muted mb-4">
      Overtime allowance percentages applied to your hours.
    </p>

    <UAlert
      color="warning"
      icon="i-lucide-info"
      class="mb-6"
      :ui="{ title: 'text-sm font-medium', description: 'text-sm' }"
    >
      <template #title>
        Disclaimer
      </template>

      <template #description>
        <p class="text-sm">
          Please refer to the
          <ULink
            to="https://jumbosupermarkten.sharepoint.com/:b:/r/sites/HumanResources/Gedeelde%20documenten/HR%20-%20Medewerkersregelingen/Supply%20Chain/Supply%20Chain%20-%20Beloning%20en%20Sociaal%20Begeleidingsregeling/01.03%20UK%20-%20WCA%20Jumbo%20Logistics%202023-2028%20version%20P7-2025.pdf?csf=1&web=1&e=PPFMm6"
            target="_blank"
          >
            Jumbo Logistics Works Council Agreement (WCA)
          </ULink>
          for the latest and correct compensation overview.
        </p>
      </template>
    </UAlert>

    <div
      v-if="pending"
      class="flex justify-center py-12"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-2xl text-muted"
      />
    </div>

    <UAlert
      v-else-if="error"
      color="error"
      icon="i-lucide-alert-circle"
      title="Failed to load allowance rates"
      :description="errorMessage"
    />

    <SettingsAllowanceTable
      v-else-if="pivotRows.length > 0"
      :rows="pivotRows"
      :on-save="updateRate"
    />
  </div>
</template>
