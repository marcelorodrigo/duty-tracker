const maxGeneration = new Map<string, number>()
const activeGenerations = new Map<string, Set<number>>()

function keyOf(key: readonly unknown[]): string {
  return JSON.stringify(key)
}

export function nextGeneration(key: readonly unknown[]): number {
  const k = keyOf(key)
  const next = (maxGeneration.get(k) ?? 0) + 1
  maxGeneration.set(k, next)

  const active = activeGenerations.get(k) ?? new Set<number>()
  active.add(next)
  activeGenerations.set(k, active)

  return next
}

export function settleGeneration(key: readonly unknown[], generation: number): boolean {
  const k = keyOf(key)
  const active = activeGenerations.get(k)
  if (!active) {
    return true
  }

  active.delete(generation)
  if (active.size === 0) {
    activeGenerations.delete(k)
    return true
  }
  return false
}
