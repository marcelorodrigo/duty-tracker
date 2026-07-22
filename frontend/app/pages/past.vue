<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

const { pastPeriods, pending, error, deleteModalOpen, deletingPeriod, fetchPeriods, openDeleteModal, closeDeleteModal, remove } = useOnCallPeriods()

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
  <AppPageShell>
    <template #navigation>
      <UButton
        to="/"
        icon="i-lucide-arrow-left"
        variant="ghost"
        color="neutral"
        aria-label="Back to active periods"
      />
    </template>

    <template #title>
      Past on-call periods
    </template>

    <!-- Loading state -->
    <div
      v-if="pending"
      class="flex justify-center py-12"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-2xl text-(--ui-text-muted)"
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
      v-else-if="pastPeriods.length === 0"
      class="py-12 text-center text-(--ui-text-muted)"
    >
      <p class="text-sm">
        No past on-call periods.
      </p>
    </div>

    <!-- Past periods list -->
    <div
      v-else
      class="space-y-3"
    >
      <OnCallPeriodCard
        v-for="period in pastPeriods"
        :key="period.id"
        :period="period"
        :on-edit="handleEdit"
        :on-delete="openDeleteModal"
      />
    </div>
  </AppPageShell>

  <!-- Delete Confirmation Modal -->
  <OnCallPeriodDeleteModal
    :open="deleteModalOpen"
    :period="deletingPeriod"
    :on-close="closeDeleteModal"
    :on-confirm="handleDeleteConfirm"
  />
</template>
