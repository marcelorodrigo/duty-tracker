<script setup lang="ts">
import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { CalendarFeedEvent } from '~/types/calendarFeed'

const { activePeriods, pastPeriods, pending, error, hasMore, sentinelRef, loadNext, deleteModalOpen, deletingPeriod, openDeleteModal, closeDeleteModal, remove, reset } = useOnCallPeriods()
const { profile } = useProfile()
const hasCalendarFeedUrl = computed(() => !!profile.value?.calendarFeedUrl)
const calendarFeed = useCalendarFeed(hasCalendarFeedUrl)

const calendarFeedPreview = computed(() => calendarFeed.preview.value ?? null)
const calendarFeedPending = computed(() => calendarFeed.pending.value)
const calendarFeedError = computed(() => calendarFeed.error.value)
const calendarFeedImporting = computed(() => calendarFeed.importing.value)
const showCalendarFeed = computed(() =>
  !hasCalendarFeedUrl.value
  || calendarFeedPending.value
  || calendarFeedError.value !== null
  || calendarFeedPreview.value !== null
)

watch(hasCalendarFeedUrl, (hasUrl) => {
  if (hasUrl) {
    calendarFeed.fetchPreview()
  }
}, { immediate: true })

function handleDeleteConfirm() {
  return remove(deletingPeriod.value!.id)
}

function handleEdit(period: OnCallPeriodResponse) {
  navigateTo(`/oncall/${period.id}/edit`)
}

function handleCalendarRefresh() {
  calendarFeed.fetchPreview()
  reset()
  void loadNext()
}

async function handleImportEvent(event: CalendarFeedEvent): Promise<boolean> {
  const ok = await calendarFeed.importEvent(event)
  if (ok) {
    reset()
    void loadNext()
  }
  return ok
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

      <!-- Past periods section -->
      <div
        v-if="pastPeriods.length > 0"
        class="mt-8 pt-6 border-t border-(--ui-border)"
      >
        <div class="flex items-center gap-2 mb-4">
          <UIcon
            name="i-lucide-history"
            class="text-(--ui-text-muted) size-4"
          />
          <h2 class="text-sm font-medium text-(--ui-text-muted)">
            Past periods
          </h2>
        </div>

        <div class="space-y-3">
          <OnCallPeriodCard
            v-for="period in pastPeriods"
            :key="period.id"
            :period="period"
            :on-edit="handleEdit"
            :on-delete="openDeleteModal"
          />
        </div>

        <div
          v-if="hasMore"
          class="flex justify-center py-4"
        >
          <UIcon
            name="i-lucide-loader-circle"
            class="animate-spin text-xl text-(--ui-text-muted)"
          />
        </div>
        <div
          v-else-if="pastPeriods.length > 0"
          class="mt-3"
        >
          <NuxtLink
            to="/past"
            class="inline-flex items-center gap-2 text-sm text-(--ui-text-muted) hover:text-(--ui-text) transition-colors"
          >
            <UIcon name="i-lucide-arrow-right" />
            <span>View all past periods</span>
          </NuxtLink>
        </div>

        <div
          ref="sentinelRef"
          class="h-px"
        />
      </div>

      <!-- Calendar feed preview section -->
      <div
        v-if="showCalendarFeed"
        class="mt-8 pt-6 border-t border-(--ui-border)"
      >
        <CalendarFeedPreview
          :preview="calendarFeedPreview"
          :pending="calendarFeedPending"
          :error="calendarFeedError"
          :has-feed-url="hasCalendarFeedUrl"
          :import-event="handleImportEvent"
          :importing="calendarFeedImporting"
          @refresh="handleCalendarRefresh"
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
