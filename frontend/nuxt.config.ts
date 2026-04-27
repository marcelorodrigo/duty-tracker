// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-01-01',
  ssr: false,
  devtools: { enabled: true },

  modules: [
    '@nuxt/ui',
    '@pinia/nuxt',
    '@vueuse/nuxt',
    'dayjs-nuxt',
  ],

  colorMode: {
    preference: 'system',
    fallback: 'light',
  },

  dayjs: {
    locales: ['nl'],
    defaultLocale: 'nl',
  },

  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8080/api/v1',
    },
  },

  css: ['~/assets/css/main.css', '~/assets/css/print.css'],
})
