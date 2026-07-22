import * as v from 'valibot'

const percentageInput = v.pipe(
  v.union([v.string(), v.number()]),
  v.transform(value => typeof value === 'number'
    ? value
    : value.trim() === '' ? Number.NaN : Number(value)),
  v.number('Enter a valid percentage'),
  v.minValue(0, 'Percentage must be 0 or more'),
  v.maxValue(100, 'Percentage must be 100 or less')
)

export const allowanceRateSchema = v.object({
  percentage: percentageInput
})

export type AllowanceRateFormData = v.InferOutput<typeof allowanceRateSchema>
