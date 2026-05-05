<script setup lang="ts">
import type { HolidayResponse, HolidayInput } from '~/types/holiday'
import { formatDate } from '~/utils/dates'

const props = defineProps<{
  periodId: number
  periodStart: string
  periodEnd: string
}>()

const { holidays, suggestions, pending, savePending, fetchHolidays, fetchSuggestions, saveHolidays } = useHolidays(props.periodId)

// Local working copy of holiday items (suggestions merged with saved)
const items = ref<HolidayInput[]>([])

// Custom holiday fields
const customDate = ref('')
const customName = ref('')
const customError = ref<string | null>(null)

onMounted(async () => {
  const startDate = props.periodStart.split('T')[0] ?? props.periodStart
  const endDate = props.periodEnd.split('T')[0] ?? props.periodEnd
  await Promise.all([
    fetchHolidays(),
    fetchSuggestions(startDate, endDate)
  ])
  buildItems()
})

function buildItems(): void {
  const savedDates = new Set(holidays.value.map(h => h.date))
  const savedMap = new Map(holidays.value.map(h => [h.date, h]))

  // Start with suggestions, marking them selected if they are already saved
  const suggestionItems: HolidayInput[] = suggestions.value.map(s => ({
    date: s.date,
    name: savedMap.get(s.date)?.name ?? s.name ?? '',
    selected: savedDates.has(s.date)
  }))

  // Add any saved holidays that are NOT in suggestions (custom ones)
  const customItems: HolidayInput[] = holidays.value
    .filter(h => !suggestions.value.some(s => s.date === h.date))
    .map(h => ({
      date: h.date,
      name: h.name ?? '',
      selected: true
    }))

  items.value = [...suggestionItems, ...customItems].sort((a, b) => a.date.localeCompare(b.date))
}

function addCustomHoliday(): void {
  customError.value = null
  if (!customDate.value) {
    customError.value = 'Date is required'
    return
  }

  // Check within period range
  const start = props.periodStart.split('T')[0] ?? props.periodStart
  const end = props.periodEnd.split('T')[0] ?? props.periodEnd
  if (customDate.value < start || customDate.value > end) {
    customError.value = 'Date must be within the on-call period'
    return
  }

  // Check for duplicates
  if (items.value.some(i => i.date === customDate.value)) {
    customError.value = 'A holiday on this date already exists'
    return
  }

  items.value = [...items.value, {
    date: customDate.value,
    name: customName.value,
    selected: true
  }].sort((a, b) => a.date.localeCompare(b.date))

  customDate.value = ''
  customName.value = ''
}

function removeItem(date: string): void {
  items.value = items.value.filter(i => i.date !== date)
}

async function save(): Promise<void> {
  const selected: HolidayResponse[] = items.value
    .filter(i => i.selected)
    .map(i => ({ date: i.date, name: i.name || null }))

  await saveHolidays(selected)
}
</script>

<template>
  <div class="border border-(--ui-border) rounded-lg p-5 mb-8">
    <h2 class="text-base font-semibold mb-4 flex items-center gap-2">
      <UIcon
        name="i-lucide-calendar-days"
        class="text-(--ui-text-muted)"
      />
      Holidays
    </h2>

    <div
      v-if="pending"
      class="flex justify-center py-4"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-xl text-(--ui-text-muted)"
      />
    </div>

    <template v-else>
      <!-- Suggestion / saved list -->
      <div
        v-if="items.length > 0"
        class="space-y-2 mb-4"
      >
        <div
          v-for="item in items"
          :key="item.date"
          class="flex items-center gap-3"
        >
          <UCheckbox
            v-model="item.selected"
            :label="formatDate(item.date)"
          />
          <UInput
            v-model="item.name"
            placeholder="Holiday name (optional)"
            size="sm"
            class="flex-1"
          />
          <UButton
            icon="i-lucide-x"
            variant="ghost"
            size="sm"
            color="neutral"
            aria-label="Remove holiday"
            @click="removeItem(item.date)"
          />
        </div>
      </div>

      <p
        v-else
        class="text-sm text-(--ui-text-muted) mb-4"
      >
        No public holidays found in this period.
      </p>

      <!-- Add custom holiday -->
      <div class="border-t border-(--ui-border) pt-4 mt-4">
        <p class="text-xs font-medium text-(--ui-text-muted) mb-2 uppercase tracking-wide">
          Add custom holiday
        </p>
        <div class="flex items-start gap-2">
          <div class="flex-shrink-0 w-40">
            <UInput
              v-model="customDate"
              type="date"
              :min="periodStart.split('T')[0]"
              :max="periodEnd.split('T')[0]"
              size="sm"
            />
          </div>
          <UInput
            v-model="customName"
            placeholder="Name (optional)"
            size="sm"
            class="flex-1"
          />
          <UButton
            icon="i-lucide-plus"
            size="sm"
            variant="outline"
            color="neutral"
            @click="addCustomHoliday"
          >
            Add
          </UButton>
        </div>
        <p
          v-if="customError"
          class="text-xs text-red-500 mt-1"
        >
          {{ customError }}
        </p>
      </div>

      <!-- Save button -->
      <div class="flex justify-end mt-4">
        <UButton
          :loading="savePending"
          icon="i-lucide-save"
          size="sm"
          @click="save"
        >
          Save holidays
        </UButton>
      </div>
    </template>
  </div>
</template>
