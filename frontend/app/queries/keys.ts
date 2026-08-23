export const QUERY_KEYS = {
  calendarFeed: {
    root: () => ['calendarFeed'] as const,
    preview: () => [...QUERY_KEYS.calendarFeed.root(), 'preview'] as const,
  },
} as const
