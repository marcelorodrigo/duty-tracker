<template>
  <div>
    <header class="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 sticky top-0 z-50 w-full">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center space-x-4">
            <NuxtLink to="/" class="flex items-center space-x-3">
              <img src="/duty-tracker.png" alt="Duty Tracker Logo" class="h-8 w-8" />
              <span class="font-bold text-xl tracking-tight text-gray-900 dark:text-white">Duty Tracker</span>
            </NuxtLink>
            
            <!-- System Navigation Topbar -->
            <nav class="hidden md:flex ml-8 space-x-4">
              <UButton variant="ghost" color="gray" to="/oncall">Periods</UButton>
              <UButton variant="ghost" color="gray" to="/report">Reports</UButton>
            </nav>
          </div>
          
          <div class="flex items-center space-x-2">
            <ClientOnly>
              <UColorModeSelect variant="ghost" color="neutral" />
              <template #fallback>
                <div class="w-8 h-8" />
              </template>
            </ClientOnly>
            <UButton variant="ghost" color="gray" to="/settings" icon="i-heroicons-cog-6-tooth" />
          </div>
        </div>
      </div>
    </header>

    <main class="min-h-screen bg-gray-50 dark:bg-gray-950 pt-6">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
// Default layout
</script>
