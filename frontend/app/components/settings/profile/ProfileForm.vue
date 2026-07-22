<script setup lang="ts">
import { toRef } from 'vue'
import type { FormSubmitEvent } from '@nuxt/ui'
import ProfileCompensationFields from './ProfileCompensationFields.vue'
import ProfileFormActions from './ProfileFormActions.vue'
import ProfileRateWarning from './ProfileRateWarning.vue'
import ProfileScheduleFields from './ProfileScheduleFields.vue'
import { profileFormSchema, type ProfileFormData } from '~/schemas/profile'
import type { EngineerProfileResponse, UpdateProfileRequest } from '~/types/profile'

interface Props {
  profile: EngineerProfileResponse
  save: (request: UpdateProfileRequest) => Promise<void>
}

const props = defineProps<Props>()

const {
  state,
  saving,
  showRateWarning,
  warningHourlyRate,
  toggleDay,
  submit,
  confirmRateWarning,
  dismissRateWarning
} = useProfileForm({
  profile: toRef(props, 'profile'),
  save: request => props.save(request)
})

async function onSubmit(event: FormSubmitEvent<ProfileFormData>): Promise<void> {
  await submit(event.data)
}
</script>

<template>
  <UForm
    :schema="profileFormSchema"
    :state="state"
    class="space-y-8"
    @submit="onSubmit"
  >
    <ProfileScheduleFields
      :working-days="state.workingDays"
      v-model:work-start-time="state.workStartTime"
      v-model:work-end-time="state.workEndTime"
      @toggle-day="toggleDay"
    />

    <ProfileCompensationFields
      v-model:hourly-rate="state.hourlyRate"
      v-model:standby-weekday-saturday-percentage="state.standbyWeekdaySaturdayPercentage"
      v-model:standby-weekday-sunday-holiday-percentage="state.standbyWeekdaySundayHolidayPercentage"
    />

    <ProfileFormActions :saving="saving" />
  </UForm>

  <ProfileRateWarning
    :open="showRateWarning"
    :hourly-rate="warningHourlyRate"
    :saving="saving"
    @cancel="dismissRateWarning"
    @confirm="confirmRateWarning"
  />
</template>
