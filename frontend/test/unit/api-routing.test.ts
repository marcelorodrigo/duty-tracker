import { readFileSync, readdirSync } from 'node:fs'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'
import { apiPath } from '~/utils/api'

const frontendRoot = fileURLToPath(new URL('../..', import.meta.url))

function readSourceTree(directory: string): string {
  return readdirSync(directory, { withFileTypes: true })
    .map(entry => entry.isDirectory()
      ? readSourceTree(join(directory, entry.name))
      : readFileSync(join(directory, entry.name), 'utf8'))
    .join('\n')
}

describe('same-origin API routing', () => {
  it('adds the API version prefix exactly once', () => {
    expect(apiPath('/profile')).toBe('/api/v1/profile')
    expect(apiPath('/oncall-periods/42/report')).toBe('/api/v1/oncall-periods/42/report')
  })

  it('keeps browser code independent from deployment host names', () => {
    const browserSource = readSourceTree(join(frontendRoot, 'app'))

    expect(browserSource).not.toContain('localhost:8080')
    expect(browserSource).not.toContain('useRuntimeConfig')
    expect(browserSource.match(/\/api\/v1/g)).toHaveLength(1)
  })

  it('forwards the unchanged API path in development and production', () => {
    const nuxtConfig = readFileSync(join(frontendRoot, 'nuxt.config.ts'), 'utf8')
    const nginxConfig = readFileSync(join(frontendRoot, 'nginx.conf'), 'utf8')
    const composeConfig = readFileSync(join(frontendRoot, '..', 'docker-compose.yml'), 'utf8')

    expect(nuxtConfig).toContain("'/api'")
    expect(nuxtConfig).toContain("target: 'http://localhost:8080'")
    expect(nuxtConfig).not.toContain('runtimeConfig')
    expect(nuxtConfig).not.toContain('NUXT_PUBLIC_API_BASE')

    expect(nginxConfig).toMatch(/location \/api\/\s*\{[\s\S]*proxy_pass http:\/\/backend:8080;/)
    expect(nginxConfig).not.toMatch(/proxy_pass http:\/\/backend:8080\//)
    expect(composeConfig).not.toContain('NUXT_PUBLIC_API_BASE')
  })
})
