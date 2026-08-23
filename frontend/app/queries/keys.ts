export const QUERY_KEYS = {
  calendarFeed: {
    root: () => ['calendarFeed'] as const,
    preview: () => [...QUERY_KEYS.calendarFeed.root(), 'preview'] as const
  },
  earnings: {
    root: () => ['earnings'] as const,
    byPeriod: (periodId: number) => [...QUERY_KEYS.earnings.root(), periodId] as const
  },
  onCallPeriodReport: {
    root: () => ['onCallPeriodReport'] as const,
    byPeriod: (periodId: number) => [...QUERY_KEYS.onCallPeriodReport.root(), periodId] as const
  },
  onCallPeriods: {
    root: () => ['onCallPeriods'] as const,
    list: () => [...QUERY_KEYS.onCallPeriods.root(), 'list'] as const
  },
  profile: {
    root: () => ['profile'] as const
  },
  compensationRates: {
    root: () => ['compensationRates'] as const
  },
  holidays: {
    root: () => ['holidays'] as const,
    byPeriod: (periodId: number) => [...QUERY_KEYS.holidays.root(), periodId] as const,
    suggestions: () => [...QUERY_KEYS.holidays.root(), 'suggestions'] as const
  }
} as const
