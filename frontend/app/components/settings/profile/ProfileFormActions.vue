<script setup lang="ts">
interface Props {
  saving: boolean
}

defineProps<Props>()
</script>

<template>
  <div class="pt-2">
    <UButton
      type="submit"
      :loading="saving"
      :disabled="saving"
      icon="i-lucide-save"
    >
      Save profile
    </UButton>
  </div>
</template>
