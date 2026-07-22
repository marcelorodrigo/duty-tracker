<script setup lang="ts">
import ProfileForm from '~/components/settings/profile/ProfileForm.vue'

const { profile, pending, error, save } = useProfile()
</script>

<template>
  <div class="max-w-2xl">
    <div
      v-if="pending"
      class="flex justify-center py-12"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="animate-spin text-2xl text-muted"
      />
    </div>

    <UAlert
      v-else-if="error"
      color="error"
      icon="i-lucide-alert-circle"
      title="Failed to load profile"
      description="Please reload the page to try again."
    />

    <div
      v-else-if="!profile"
      class="py-12 text-center text-muted"
    >
      <p class="text-sm">
        No profile found.
      </p>
    </div>

    <ProfileForm
      v-else
      :profile="profile"
      :save="save"
    />
  </div>
</template>
