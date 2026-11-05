import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'
import { defineVitestProject } from '@nuxt/test-utils/config'

export default defineConfig({
  test: {
    projects: [
      {
        resolve: {
          alias: {
            '~': fileURLToPath(new URL('./app', import.meta.url))
          }
        },
        test: {
          name: 'unit',
          include: ['test/unit/*.{test,spec}.ts'],
          environment: 'node'
        }
      },
      await defineVitestProject({
        test: {
          name: 'nuxt',
          include: ['test/nuxt/*.{test,spec}.ts'],
          environment: 'nuxt',
          environmentOptions: {
            nuxt: {
              rootDir: fileURLToPath(new URL('.', import.meta.url)),
              domEnvironment: 'happy-dom'
            }
          }
        }
      })
    ],
    coverage: {
      provider: 'v8',
      reporter: ['lcov', 'text'],
      include: ['app/**/*.{ts,vue}'],
      exclude: [
        'app/types/**',
        'app/app.config.ts'
      ],
      reportsDirectory: './coverage'
    },
    onConsoleLog(log) {
      // Suppress Vue Suspense experimental feature warning from @nuxt/test-utils
      if (log.includes('<Suspense> is an experimental feature')) {
        return false
      }
    }
  }
})
