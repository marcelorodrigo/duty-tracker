<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'

const { pastPeriods, pending, error, refresh, deleteModalOpen, deletingPeriod, openDeleteModal, closeDeleteModal, remove } = useOnCallPeriods()

onMounted(refresh)

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
        <div class="flex items-center gap-2">
          <NuxtLink to="/">
            <UButton
              icon="i-lucide-arrow-left"
              variant="ghost"
              color="neutral"
              aria-label="Back to active periods"
            />
          </NuxtLink>
          <h1 class="text-2xl font-semibold">
            Past on-call periods
          </h1>
        </div>
      </div>

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
