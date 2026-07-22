<script setup lang="ts">
import { toRef } from 'vue'
import ProfileCompensationFields from './ProfileCompensationFields.vue'
import ProfileFormActions from './ProfileFormActions.vue'
import ProfileRateWarning from './ProfileRateWarning.vue'
import ProfileScheduleFields from './ProfileScheduleFields.vue'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'

interface Props {
  profile: EngineerProfileResponse
  save: (request: UpdateProfileRequest) => Promise<void>
}

const props = defineProps<Props>()

const {
  workingDays,
  workStartTime,
  workEndTime,
  hourlyRate,
  standbyWeekdaySaturdayPercentage,
  standbyWeekdaySundayHolidayPercentage,
  saving,
  showRateWarning,
  submitAttempted,
  rateError,
  standbyWeekdaySaturdayError,
  standbyWeekdaySundayHolidayError,
  toggleDay,
  submit,
  confirmRateWarning,
  dismissRateWarning
} = useProfileForm({
  profile: toRef(props, 'profile'),
  save: request => props.save(request)
})
</script>

<template>
  <form
    class="space-y-8"
    @submit.prevent="submit"
  >
    <ProfileScheduleFields
      :working-days="workingDays"
      v-model:work-start-time="workStartTime"
      v-model:work-end-time="workEndTime"
      @toggle-day="toggleDay"
    />

    <ProfileCompensationFields
      v-model:hourly-rate="hourlyRate"
      v-model:standby-weekday-saturday-percentage="standbyWeekdaySaturdayPercentage"
      v-model:standby-weekday-sunday-holiday-percentage="standbyWeekdaySundayHolidayPercentage"
      :submit-attempted="submitAttempted"
      :rate-error="rateError"
      :standby-weekday-saturday-error="standbyWeekdaySaturdayError"
      :standby-weekday-sunday-holiday-error="standbyWeekdaySundayHolidayError"
    />

    <ProfileFormActions :saving="saving" />
  </form>

  <ProfileRateWarning
    :open="showRateWarning"
    :hourly-rate="hourlyRate"
    :saving="saving"
    @cancel="dismissRateWarning"
    @confirm="confirmRateWarning"
  />
</template>
