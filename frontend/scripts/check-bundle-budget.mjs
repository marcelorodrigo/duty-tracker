import { readFile, readdir, stat } from 'node:fs/promises'
import { resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const ASSET_TYPES = {
  javascript: {
    extension: '.js',
    label: 'JavaScript'
  },
  stylesheets: {
    extension: '.css',
    label: 'CSS'
  }
}

async function listFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true })
  const files = await Promise.all(entries.map(async (entry) => {
    const path = resolve(directory, entry.name)

    return entry.isDirectory() ? listFiles(path) : path
  }))

  return files.flat()
}

export async function measureBundle(directory) {
  const files = await listFiles(directory)
  const measurements = {}

  for (const [name, definition] of Object.entries(ASSET_TYPES)) {
    const assetFiles = files.filter(file => file.endsWith(definition.extension))
    const sizes = await Promise.all(assetFiles.map(async file => (await stat(file)).size))

    measurements[name] = {
      bytes: sizes.reduce((total, size) => total + size, 0),
      files: assetFiles.length
    }
  }

  return measurements
}

function requireNonNegativeNumber(value, path) {
  if (typeof value !== 'number' || !Number.isFinite(value) || value < 0) {
    throw new Error(`${path} must be a non-negative number`)
  }

  return value
}

export function validateBudget(budget) {
  if (budget?.schemaVersion !== 1) {
    throw new Error('schemaVersion must be 1')
  }

  const tolerancePercent = requireNonNegativeNumber(
    budget.tolerancePercent,
    'tolerancePercent'
  )

  for (const assetType of Object.keys(ASSET_TYPES)) {
    requireNonNegativeNumber(
      budget.assets?.[assetType]?.baselineBytes,
      `assets.${assetType}.baselineBytes`
    )
  }

  return {
    assets: budget.assets,
    tolerancePercent
  }
}

export function evaluateBundle(measurements, budget) {
  const validatedBudget = validateBudget(budget)

  return Object.entries(ASSET_TYPES).map(([name, definition]) => {
    const measurement = measurements[name]
    const baselineBytes = validatedBudget.assets[name].baselineBytes
    const maximumBytes = Math.floor(
      baselineBytes * (100 + validatedBudget.tolerancePercent) / 100
    )

    if (!measurement || measurement.files === 0) {
      throw new Error(`No generated ${definition.label} assets were found`)
    }

    return {
      name,
      label: definition.label,
      actualBytes: measurement.bytes,
      baselineBytes,
      maximumBytes,
      files: measurement.files,
      exceeded: measurement.bytes > maximumBytes
    }
  })
}

function parseArguments(arguments_) {
  const options = {
    budget: 'bundle-budget.json',
    output: '.output/public/_nuxt'
  }

  for (let index = 0; index < arguments_.length; index += 1) {
    const argument = arguments_[index]
    const value = arguments_[index + 1]

    if ((argument === '--budget' || argument === '--output') && value) {
      options[argument.slice(2)] = value
      index += 1
      continue
    }

    throw new Error(`Unknown or incomplete argument: ${argument}`)
  }

  return options
}

async function run() {
  const options = parseArguments(process.argv.slice(2))
  const budgetPath = resolve(options.budget)
  const outputPath = resolve(options.output)
  const budget = JSON.parse(await readFile(budgetPath, 'utf8'))
  const measurements = await measureBundle(outputPath)
  const results = evaluateBundle(measurements, budget)

  for (const result of results) {
    const status = result.exceeded ? 'OVER BUDGET' : 'within budget'
    console.log(
      `${result.label}: ${result.actualBytes.toLocaleString()} bytes across `
      + `${result.files} files; baseline ${result.baselineBytes.toLocaleString()} bytes, `
      + `limit ${result.maximumBytes.toLocaleString()} bytes (${status})`
    )
  }

  if (results.some(result => result.exceeded)) {
    process.exitCode = 1
  }
}

const executedPath = process.argv[1] ? resolve(process.argv[1]) : undefined

if (executedPath === fileURLToPath(import.meta.url)) {
  run().catch((error) => {
    console.error(`Bundle budget check failed: ${error.message}`)
    process.exitCode = 1
  })
}
