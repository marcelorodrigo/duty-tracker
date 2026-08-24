const generations = new Map<string, number>()

function keyOf(key: readonly unknown[]): string {
  return JSON.stringify(key)
}

export function nextGeneration(key: readonly unknown[]): number {
  const k = keyOf(key)
  const next = (generations.get(k) ?? 0) + 1
  generations.set(k, next)
  return next
}

export function isLatestGeneration(key: readonly unknown[], generation: number): boolean {
  return generations.get(keyOf(key)) === generation
}
