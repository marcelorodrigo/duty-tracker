<template>
  <div class="space-y-6">
    <UCard variant="subtle">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h3 class="font-medium text-gray-900 dark:text-white">Color Scheme</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">Choose how Duty Tracker looks to you.</p>
        </div>
        <UColorModeSelect />
      </div>
    </UCard>
    
    <UAlert v-if="error" color="error" :description="error" />

    <div class="flex justify-end pt-4 border-t border-gray-200 dark:border-gray-800">
      <UButton @click="onSave" :loading="loading" trailing-icon="i-lucide-arrow-right" color="primary">
        Save & Continue
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{ saved: [] }>()
const prefsStore = usePreferencesStore()
const colorMode = useColorMode()
const loading = ref(false)
const error = ref<string | null>(null)

async function onSave() {
  loading.value = true
  error.value = null
  try {
    const schemeMap: Record<string, 'DARK' | 'LIGHT' | 'AUTO'> = {
      dark: 'DARK', light: 'LIGHT', system: 'AUTO'
    }
    const colorScheme = schemeMap[colorMode.preference] ?? 'AUTO'
    await prefsStore.updatePreferences({ colorScheme })
    emit('saved')
  } catch {
    // Error is already handled by useApi().put() which shows a toast
  } finally {
    loading.value = false
  }
}
</script>
