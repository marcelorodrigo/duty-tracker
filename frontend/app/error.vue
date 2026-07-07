<script setup lang="ts">
import type { NuxtError } from '#app'

defineProps<{ error: NuxtError }>()

useSeoMeta({
  title: 'Error - Duty Tracker'
})
</script>

<template>
  <UError
    :error="error"
    icon="i-lucide-alert-triangle"
    redirect="/"
  >
    <template #statusMessage>
      {{ error.statusCode === 404 ? 'Page not found' : 'Something went wrong' }}
    </template>
    <template #message>
      {{ error.statusCode === 404 ? "The page you're looking for doesn't exist or has been moved." : 'An unexpected error occurred. Please try again later.' }}
    </template>
  </UError>
</template>
