<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

const { activePeriods, pastPeriods, pending, error, deleteModalOpen, deletingPeriod, fetchPeriods, openDeleteModal, closeDeleteModal, remove } = useOnCallPeriods()

onMounted(() => {
  fetchPeriods()
})

function handleDeleteConfirm() {
  return remove(deletingPeriod.value!.id)
}

function handleEdit(period: OnCallPeriodResponse) {
  navigateTo(`/oncall/${period.id}/edit`)
}
</script>

<template>
  <UContainer>
    <div class="py-6">
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-semibold">
          On-call periods
        </h1>
        <UButton
          icon="i-lucide-plus"
          to="/oncall/new"
        >
          New on-call
        </UButton>
      </div>

      <!-- Loading state -->
      <div
        v-if="pending"
        class="flex justify-center py-12"
      >
        <UIcon
          name="i-lucide-loader-circle"
          class="animate-spin text-2xl text-muted"
        />
      </div>

      <!-- Error state -->
      <UAlert
        v-else-if="error"
        color="error"
        icon="i-lucide-alert-circle"
        title="Failed to load periods"
        description="Please reload the page to try again."
      />

      <!-- Empty state -->
      <div
        v-else-if="activePeriods.length === 0"
        class="py-12 text-center space-y-4"
      >
        <UIcon
          name="i-lucide-calendar-off"
          class="text-4xl text-muted mx-auto"
        />
        <div class="space-y-2">
          <p class="text-sm font-medium">
            No active on-call periods
          </p>
          <p class="text-xs text-muted">
            Make sure your profile is configured, then create your first on-call period.
          </p>
        </div>
        <div class="flex gap-2 justify-center pt-2">
          <UButton
            to="/settings"
            variant="outline"
            icon="i-lucide-settings"
          >
            Configure profile
          </UButton>
          <UButton
            icon="i-lucide-plus"
            to="/oncall/new"
          >
            Create on-call period
          </UButton>
        </div>
      </div>

      <!-- Active periods list -->
      <div
        v-else
        class="space-y-3"
      >
        <OnCallPeriodCard
          v-for="period in activePeriods"
          :key="period.id"
          :period="period"
          :on-edit="handleEdit"
          :on-delete="openDeleteModal"
        />
      </div>

      <!-- Link to past periods -->
      <div
        v-if="pastPeriods.length > 0"
        class="mt-8 pt-6 border-t border-(--ui-border)"
      >
        <NuxtLink
          to="/past"
          class="inline-flex items-center gap-2 text-sm text-(--ui-text-muted) hover:text-(--ui-text) transition-colors"
        >
          <UIcon name="i-lucide-history" />
          <span>View {{ pastPeriods.length }} past {{ pastPeriods.length === 1 ? 'period' : 'periods' }}</span>
        </NuxtLink>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <OnCallPeriodDeleteModal
      :open="deleteModalOpen"
      :period="deletingPeriod"
      :on-close="closeDeleteModal"
      :on-confirm="handleDeleteConfirm"
    />
  </UContainer>
</template>
