import { defineConfig, devices } from '@playwright/test'

const port = 3195
const baseURL = `http://127.0.0.1:${port}`

export default defineConfig({
  testDir: './test/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: process.env.CI ? 'github' : 'list',
  use: {
    ...devices['Desktop Chrome'],
    baseURL,
    locale: 'en-GB',
    timezoneId: 'Europe/Amsterdam',
    trace: 'retain-on-failure'
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ],
  webServer: {
    command: `pnpm dev --host 127.0.0.1 --port ${port}`,
    env: {
      NUXT_PUBLIC_API_BASE: baseURL
    },
    url: baseURL,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000
  }
})
