<script setup lang="ts">
import type { EmployeeType } from '~/types/compensation'

const { profile } = useProfile()
// employeeType is null until profile resolves, then becomes INTERNAL or EXTERNAL
const employeeType = computed<EmployeeType | null>(() => profile.value?.employeeType ?? null)
provide('employeeType', employeeType)

const route = useRoute()

const tabs = [
  { label: 'Allowance', to: '/settings/allowance' },
  { label: 'Profile', to: '/settings/profile' }
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
