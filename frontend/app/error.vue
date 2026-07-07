<script setup lang="ts">
import type { NuxtError } from '#app'

defineProps<{ error: NuxtError }>()

useSeoMeta({
  title: 'Error - Duty Tracker'
})
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-dvh p-8 text-center space-y-6">
    <div class="space-y-2">
      <p class="text-[10rem] font-bold leading-none text-(--ui-text-muted)">
        {{ error.status }}
      </p>
      <h1 class="text-xl font-semibold text-(--ui-text)">
        {{ error.status === 404 ? 'Page not found' : 'Something went wrong' }}
      </h1>
      <p class="text-sm text-(--ui-text-muted) max-w-md">
        {{ error.status === 404 ? 'The page you\'re looking for doesn\'t exist or has been moved.' : 'An unexpected error occurred. Please try again later.' }}
      </p>
    </div>
    <NuxtLink
      to="/"
      class="inline-flex items-center gap-2 rounded-xl bg-(--ui-primary) px-4 py-2 text-sm font-medium text-(--ui-primary-fg) hover:bg-(--ui-primary-hover) transition-colors"
    >
      <UIcon
        name="i-lucide-arrow-left"
        class="size-4"
      />
      Back to Home
    </NuxtLink>
  </div>
</template>
