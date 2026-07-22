import { spawnSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const repositoryRoot = fileURLToPath(new URL('../..', import.meta.url))
const worktreeProbe = spawnSync(
  'git',
  ['-C', repositoryRoot, 'rev-parse', '--is-inside-work-tree'],
  { stdio: 'ignore' }
)

if (worktreeProbe.status === 0) {
  const hookSetup = spawnSync(
    'git',
    ['-C', repositoryRoot, 'config', 'core.hooksPath', '.githooks'],
    { stdio: 'inherit' }
  )

  if (hookSetup.status !== 0) {
    process.exit(hookSetup.status ?? 1)
  }
}
