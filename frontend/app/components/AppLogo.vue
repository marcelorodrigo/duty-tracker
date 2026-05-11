<script setup lang="ts">
import engineerDark from '~/assets/engineer-dark.svg'
import engineerLight from '~/assets/engineer-light.svg'

const colorMode = useColorMode()
const logo = computed(() => colorMode.value === 'dark' ? engineerDark : engineerLight)
</script>

<template>
  <span class="flex items-center gap-2 text-lg font-semibold tracking-tight">
    <img :src="logo" alt="" class="size-8" aria-hidden="true" />
    Duty Tracker
  </span>
</template>
