<script setup lang="ts">
interface Props {
  maxWidth?: 'default' | 'narrow'
}

const props = withDefaults(defineProps<Props>(), {
  maxWidth: 'default'
})

defineSlots<{
  navigation?(): unknown
  title(): unknown
  subtitle?(): unknown
  actions?(): unknown
  default(): unknown
}>()

const contentClasses = computed(() => ({
  'mx-auto max-w-3xl': props.maxWidth === 'narrow'
}))
</script>

<template>
  <UContainer>
    <div
      class="py-6"
      :class="contentClasses"
    >
      <header class="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex min-w-0 items-center gap-2">
          <nav
            v-if="$slots.navigation"
            aria-label="Page navigation"
            class="shrink-0"
          >
            <slot name="navigation" />
          </nav>

          <div class="min-w-0">
            <h1 class="text-2xl font-semibold text-(--ui-text)">
              <slot name="title" />
            </h1>
            <p
              v-if="$slots.subtitle"
              class="mt-1 text-sm text-(--ui-text-muted)"
            >
              <slot name="subtitle" />
            </p>
          </div>
        </div>

        <div
          v-if="$slots.actions"
          class="flex flex-wrap items-center gap-2 sm:justify-end"
        >
          <slot name="actions" />
        </div>
      </header>

      <slot />
    </div>
  </UContainer>
</template>
