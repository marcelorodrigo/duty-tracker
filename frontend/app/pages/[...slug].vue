<script setup lang="ts">
</script>

<template>
  <UContainer>
    <div class="py-12 text-center space-y-6">
      <UIcon
        name="i-lucide-file-question"
        class="text-6xl text-(--ui-text-muted) mx-auto"
      />

      <div class="space-y-2">
        <h1 class="text-2xl font-semibold text-(--ui-text)">
          Page not found
        </h1>
        <p class="text-sm text-(--ui-text-muted) max-w-md mx-auto">
          The page you're looking for doesn't exist or has been moved.
        </p>
      </div>

      <UButton
        to="/"
        icon="i-lucide-arrow-left"
      >
        Go to Dashboard
      </UButton>
    </div>
  </UContainer>
</template>
