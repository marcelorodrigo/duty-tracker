import { expect, test } from '@playwright/test'
import type { Route } from '@playwright/test'

const profile = {
  id: 1,
  workingDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
  workStartTime: '08:00:00',
  workEndTime: '16:30:00',
  hourlyRate: 50,
  standbyWeekdaySaturdayPercentage: 0.067,
  standbyWeekdaySundayHolidayPercentage: 0.084
}

const period = {
  id: 42,
  startDateTime: '2020-01-01T14:00:00',
  endDateTime: '2020-01-08T14:00:00',
  holidays: [],
  createdAt: '2020-01-01T10:00:00Z'
}

async function respond(route: Route, body: unknown): Promise<void> {
  await route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify(body)
  })
}

async function abortUnexpectedRequest(route: Route): Promise<void> {
  const request = route.request()
  throw new Error(`Unexpected API request: ${request.method()} ${request.url()}`)
}

test('updates the profile through the browser and API boundary', async ({ page }) => {
  await page.route('**/api/v1/profile', async (route) => {
    if (route.request().method() === 'GET') {
      await respond(route, profile)
      return
    }

    if (route.request().method() === 'PUT') {
      await respond(route, {
        ...profile,
        ...route.request().postDataJSON()
      })
      return
    }

    await abortUnexpectedRequest(route)
  })

  await page.goto('/settings/profile')
  await expect(page.getByRole('heading', { name: 'Settings' })).toBeVisible()

  await page.getByLabel('Hourly rate').fill('72.50')
  await page.getByRole('button', { name: 'Sat', exact: true }).click()

  const updateRequest = page.waitForRequest(request => (
    request.method() === 'PUT'
    && new URL(request.url()).pathname === '/api/v1/profile'
  ))
  await page.getByRole('button', { name: 'Save profile' }).click()

  await expect(page.getByText('Profile saved', { exact: true })).toBeVisible()
  expect((await updateRequest).postDataJSON()).toMatchObject({
    workingDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'],
    workStartTime: '08:00:00',
    workEndTime: '16:30:00',
    hourlyRate: 72.5
  })
})

test('creates an on-call period and opens its detail page', async ({ page }) => {
  let createBody: Record<string, unknown> | undefined
  let holidaysBody: unknown

  await page.route('**/api/v1/**', async (route) => {
    const request = route.request()
    const path = new URL(request.url()).pathname

    if (request.method() === 'GET' && path === '/api/v1/holidays/suggestions') {
      await respond(route, [])
      return
    }

    if (request.method() === 'POST' && path === '/api/v1/oncall-periods') {
      createBody = request.postDataJSON() as Record<string, unknown>
      await respond(route, { id: period.id })
      return
    }

    if (request.method() === 'PUT' && path === `/api/v1/oncall-periods/${period.id}/holidays`) {
      holidaysBody = request.postDataJSON()
      await respond(route, [])
      return
    }

    if (request.method() === 'GET' && path === `/api/v1/oncall-periods/${period.id}`) {
      await respond(route, period)
      return
    }

    if (request.method() === 'GET' && path === '/api/v1/incidents') {
      await respond(route, { incidents: [] })
      return
    }

    await abortUnexpectedRequest(route)
  })

  await page.goto('/oncall/new')
  await expect(page.getByRole('heading', { name: 'New on-call period' })).toBeVisible()
  await page.getByRole('button', { name: 'Save', exact: true }).click()

  await expect(page).toHaveURL(`/oncall/${period.id}`)
  await expect(page.getByRole('heading', { name: 'On-call period' })).toBeVisible()
  expect(createBody).toMatchObject({
    startDateTime: expect.stringMatching(/^\d{4}-\d{2}-\d{2}T14:00:00$/),
    endDateTime: expect.stringMatching(/^\d{4}-\d{2}-\d{2}T14:00:00$/)
  })
  expect(holidaysBody).toEqual([])
})

test('opens earnings from a completed period and renders server totals', async ({ page }) => {
  await page.route('**/api/v1/**', async (route) => {
    const request = route.request()
    const path = new URL(request.url()).pathname

    if (request.method() === 'GET' && path === `/api/v1/oncall-periods/${period.id}`) {
      await respond(route, period)
      return
    }

    if (request.method() === 'GET' && path === '/api/v1/incidents') {
      await respond(route, { incidents: [] })
      return
    }

    if (request.method() === 'GET' && path === `/api/v1/oncall-periods/${period.id}/earnings`) {
      await respond(route, {
        periodId: period.id,
        periodStart: period.startDateTime,
        periodEnd: period.endDateTime,
        standbyLines: [
          {
            date: '2020-01-02',
            dayLabel: 'Thursday',
            compensationLabel: 'Weekday standby',
            hours: '16.0',
            amount: '125.00',
            capped: false
          }
        ],
        incidentLines: [
          {
            incidentId: 7,
            incidentName: 'Database recovery',
            hoursSummary: '2h overtime',
            subtotal: '75.00'
          }
        ],
        grandTotal: '200.00'
      })
      return
    }

    await abortUnexpectedRequest(route)
  })

  await page.goto(`/oncall/${period.id}`)
  await expect(page.getByRole('heading', { name: 'On-call period' })).toBeVisible()

  const earningsRequest = page.waitForRequest(request => (
    request.method() === 'GET'
    && new URL(request.url()).pathname === `/api/v1/oncall-periods/${period.id}/earnings`
  ))
  await page.getByRole('link', { name: 'My Earnings' }).click()

  await earningsRequest
  await expect(page).toHaveURL(`/oncall/${period.id}/earnings`)
  await expect(page.getByRole('heading', { name: 'My Earnings' })).toBeVisible()
  await expect(page.getByText('Weekday standby')).toBeVisible()
  await expect(page.getByText('Database recovery')).toBeVisible()
  await expect(page.getByText('€200.00')).toBeVisible()
})
