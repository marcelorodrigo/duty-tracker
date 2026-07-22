import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { afterEach, describe, expect, it } from 'vitest'
import {
  evaluateBundle,
  measureBundle,
  validateBudget
} from '../../scripts/check-bundle-budget.mjs'

const temporaryDirectories: string[] = []

function budget(javascriptBytes = 100, stylesheetBytes = 100) {
  return {
    schemaVersion: 1,
    tolerancePercent: 10,
    assets: {
      javascript: { baselineBytes: javascriptBytes },
      stylesheets: { baselineBytes: stylesheetBytes }
    }
  }
}

afterEach(async () => {
  await Promise.all(temporaryDirectories.splice(0).map(directory => (
    rm(directory, { recursive: true, force: true })
  )))
})

describe('bundle budget', () => {
  it('measures generated JavaScript and CSS without maps or other assets', async () => {
    const outputDirectory = await mkdtemp(join(tmpdir(), 'duty-tracker-bundle-'))
    temporaryDirectories.push(outputDirectory)
    await mkdir(join(outputDirectory, 'nested'))
    await Promise.all([
      writeFile(join(outputDirectory, 'entry.hash.js'), '12345'),
      writeFile(join(outputDirectory, 'entry.hash.js.map'), 'ignored source map'),
      writeFile(join(outputDirectory, 'entry.hash.css'), '123'),
      writeFile(join(outputDirectory, 'nested', 'chunk.hash.js'), '1234567'),
      writeFile(join(outputDirectory, 'logo.svg'), 'ignored asset')
    ])

    await expect(measureBundle(outputDirectory)).resolves.toEqual({
      javascript: { bytes: 12, files: 2 },
      stylesheets: { bytes: 3, files: 1 }
    })
  })

  it('allows assets up to and including ten percent above baseline', () => {
    const results = evaluateBundle({
      javascript: { bytes: 110, files: 2 },
      stylesheets: { bytes: 110, files: 1 }
    }, budget())

    expect(results).toEqual([
      expect.objectContaining({ name: 'javascript', maximumBytes: 110, exceeded: false }),
      expect.objectContaining({ name: 'stylesheets', maximumBytes: 110, exceeded: false })
    ])
  })

  it('flags a significant regression above ten percent', () => {
    const results = evaluateBundle({
      javascript: { bytes: 111, files: 2 },
      stylesheets: { bytes: 100, files: 1 }
    }, budget())

    expect(results.find(result => result.name === 'javascript')).toMatchObject({
      maximumBytes: 110,
      exceeded: true
    })
  })

  it('rejects invalid or incomplete budget configuration', () => {
    expect(() => validateBudget({
      schemaVersion: 1,
      tolerancePercent: 10,
      assets: {}
    })).toThrow('assets.javascript.baselineBytes must be a non-negative number')
  })

  it('rejects an empty generated asset category', () => {
    expect(() => evaluateBundle({
      javascript: { bytes: 0, files: 0 },
      stylesheets: { bytes: 100, files: 1 }
    }, budget())).toThrow('No generated JavaScript assets were found')
  })
})
