<template>
  <div class="space-y-4">
    <div class="flex flex-col gap-2">
      <label class="font-medium">Color Scheme</label>
      <UColorModeSelect />
    </div>
    <UButton @click="onSave" :loading="loading">Save & Continue</UButton>
    <UAlert v-if="error" color="error" :description="error" class="mt-2" />
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
    error.value = 'Failed to save preferences.'
  } finally {
    loading.value = false
  }
}
</script>
