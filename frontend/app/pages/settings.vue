<script setup lang="ts">
const route = useRoute()

const tabs = [
  { label: 'Profile', to: '/settings/profile' },
  { label: 'Allowance', to: '/settings/allowance' }
]

function isActive(to: string): boolean {
  return route.path === to
}
</script>

<template>
  <UContainer>
    <div class="py-6">
      <h1 class="text-2xl font-semibold mb-6">
        Settings
      </h1>

      <div class="flex gap-1 border-b border-(--ui-border) mb-6">
        <NuxtLink
          v-for="tab in tabs"
          :key="tab.to"
          :to="tab.to"
          class="px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors"
          :class="isActive(tab.to)
            ? 'border-(--ui-color-primary-500) text-(--ui-color-primary-500)'
            : 'border-transparent text-(--ui-text-muted) hover:text-(--ui-text)'"
        >
          {{ tab.label }}
        </NuxtLink>
      </div>

      <NuxtPage />
    </div>
  </UContainer>
</template>
