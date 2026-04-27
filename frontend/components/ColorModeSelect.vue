<script setup lang="ts">
const colorMode = useColorMode()
const appConfig = useAppConfig()

const modes = ['system', 'light', 'dark']

const currentIcon = computed(() => {
  if (colorMode.preference === 'system') return appConfig.ui.icons.system
  if (colorMode.preference === 'light') return appConfig.ui.icons.light
  if (colorMode.preference === 'dark') return appConfig.ui.icons.dark
  return appConfig.ui.icons.system
})

const toggleMode = () => {
  const currentIndex = modes.indexOf(colorMode.preference)
  const nextIndex = (currentIndex + 1) % modes.length
  colorMode.preference = modes[nextIndex]
}
</script>

<template>
  <ClientOnly>
    <UButton
      :icon="currentIcon"
      variant="ghost"
      color="neutral"
      aria-label="Toggle theme"
      @click="toggleMode"
    />
    <template #fallback>
      <div class="w-8 h-8" />
    </template>
  </ClientOnly>
</template>
