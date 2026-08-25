# Frontend State Management — Pinia + Pinia Colada

The frontend uses **Pinia** for global/client state and **Pinia Colada** for
server state (queries and mutations). This document describes the established
pattern so new features follow the same conventions.

## Packages

- `pinia` + `@pinia/nuxt` — store infrastructure.
- `@pinia/colada` + `@pinia/colada-nuxt` — query/mutation layer.

Both modules are registered in `nuxt.config.ts`.

## Layering

- `app/queries/*` — declarative **query and mutation definitions**. No component
  logic lives here; these are pure data-access definitions.
- `app/composables/*` — thin **consumer wrappers**. Components import these, not
  the raw `app/queries` definitions. A composable adapts Colada's API into the
  shape a component needs (e.g. `pending`/`error` computed refs, typed helpers).

Components should not call `useQuery`/`useMutation` directly; go through the
composable so the pattern stays consistent and swappable.

## Query keys

All keys live in `app/queries/keys.ts` as a single `QUERY_KEYS` object. Use a
nested, tree-shaped structure so invalidation can target a whole branch:

```ts
export const QUERY_KEYS = {
  onCallPeriods: {
    root: () => ['onCallPeriods'] as const,
    list: () => [...QUERY_KEYS.onCallPeriods.root(), 'list'] as const,
  },
} as const
```

Invalidate a whole resource with `root()`, or a specific query with its leaf key.

## Defining a query

In `app/queries/<resource>.ts`:

```ts
import { defineQueryOptions } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'

export const onCallPeriodsListQuery = defineQueryOptions({
  key: QUERY_KEYS.onCallPeriods.list(),
  query: () =>
    $fetch<{ periods: OnCallPeriodResponse[] }>('/api/v1/oncall-periods', {
      baseURL: useRuntimeConfig().public.apiBase,
    }),
})
```

## Consuming a query in a composable

```ts
import { computed, toValue, type MaybeRefOrGetter } from 'vue'
import { useQuery } from '@pinia/colada'
import { onCallPeriodsListQuery } from '~/queries/onCallPeriods'

export function useOnCallPeriods(enabled: MaybeRefOrGetter<boolean> = true) {
  const {
    state,
    data,
    asyncStatus,
    refresh,
  } = useQuery(() => ({
    ...onCallPeriodsListQuery,
    enabled: toValue(enabled),
  }))

  const pending = computed(() => asyncStatus.value === 'loading')
  const error = computed(() => state.value.error)

  return { data, pending, error, refresh }
}
```

`asyncStatus` is `'loading' | 'error' | 'success'`. Wrap it in a `computed` to
expose a friendly `pending` flag for components.

## Defining + consuming a mutation

In `app/queries/<resource>.ts`:

```ts
import { defineMutation, useQueryCache } from '@pinia/colada'
import { QUERY_KEYS } from '~/queries/keys'

export const useDeleteOnCallPeriod = defineMutation(() => {
  const queryCache = useQueryCache()

  return useMutation({
    mutation: (id: number) =>
      $fetch(`/api/v1/oncall-periods/${id}`, {
        baseURL: useRuntimeConfig().public.apiBase,
        method: 'DELETE',
      }),
    onSuccess: () => {
      queryCache.invalidateQueries({
        key: QUERY_KEYS.onCallPeriods.list(),
        exact: true,
      })
    },
  })
})
```

- Call `useQueryCache().invalidateQueries({ key, exact })` in `onSuccess` to
  refetch affected queries after a write.
- Use `exact: true` when the key fully identifies one query; omit or use a
  partial key to invalidate a whole branch (e.g. `root()`).
- Surface toasts/errors in `onError` via `useToast()` and `extractErrorDetail`.

Consume from a composable or component:

```ts
const { mutateAsync, asyncStatus } = useDeleteOnCallPeriod()
const deleting = computed(() => asyncStatus.value === 'loading')

await mutateAsync(id)
```

## Binding pending/error in components

Components read the composable's `pending`/`error` (or `importing`/`saving`/
`deleting`) computed refs to drive UI state — spinners, disable buttons, show
error banners. Do not manage `ref`/`pending`/`error` by hand for server data;
let Colada own it.

## Form state vs. server state

Per-feature **form/local UI state** (date ranges, input values, validation
errors, multi-step flow) stays in plain `ref`/`shallowRef` inside the composable
(e.g. `useOnCallPeriodForm`). Only the **save/load round-trips** go through
Colada queries/mutations. This keeps optimistic, interactive editing independent
of server-state caching.
