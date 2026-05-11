## 1. Database & Domain Model

- [ ] 1.1 Add hourly_rate column to V1__create_schema.sql (merged into initial schema, no separate migration)
- [ ] 1.2 Update EngineerProfile domain record to include BigDecimal hourlyRate field
- [ ] 1.3 Update EngineerProfileEntity JPA to add hourly_rate column mapping
- [ ] 1.4 Verify migration runs without errors in local dev environment

## 2. Backend Exceptions & Validators

- [ ] 2.1 Create InvalidHourlyRateException extending base domain exception
- [ ] 2.2 Create or update UpdateEngineerProfileRequestValidator to validate hourlyRate > BigDecimal.ONE
- [ ] 2.3 Create or update CreateEngineerProfileRequestValidator to validate hourlyRate > BigDecimal.ONE
- [ ] 2.4 Add validator tests for both valid and invalid hourly rates

## 3. Backend Use Cases

- [ ] 3.1 Update UpdateEngineerProfileUseCase to accept and validate hourlyRate
- [ ] 3.2 Update UpdateEngineerProfileUseCase to throw InvalidHourlyRateException on invalid rate
- [ ] 3.3 Update CreateEngineerProfileUseCase to accept hourlyRate with validation
- [ ] 3.4 Update request/response models (DTO) to include hourlyRate field
- [ ] 3.5 Add unit tests for UpdateEngineerProfileUseCase with hourly rate scenarios
- [ ] 3.6 Add unit tests for CreateEngineerProfileUseCase with hourly rate scenarios

## 4. Backend Mappers & API Response

- [ ] 4.1 Update EngineerProfileMapper to map hourly_rate column
- [ ] 4.2 Verify GET /api/engineer-profile returns hourlyRate in response
- [ ] 4.3 Add integration tests for update/create profile with hourly rate via API

## 5. Frontend Form & Validation

- [ ] 5.1 Add hourly rate numeric input field to engineer profile edit form
- [ ] 5.2 Implement frontend validation: hourlyRate must be > 1
- [ ] 5.3 Implement warning: if hourlyRate > 200, show "Unusual hourly rate. Continue?" modal
- [ ] 5.4 Update form submission to include hourlyRate in update payload
- [ ] 5.5 Update profile display to show current hourly rate

## 6. Frontend Testing

- [ ] 6.1 Add unit test: validate hourlyRate > 1 check
- [ ] 6.2 Add unit test: warning modal appears for > 200
- [ ] 6.3 Add unit test: form rejects submission with hourlyRate <= 1
- [ ] 6.4 Add integration test: successful profile update with new hourly rate

## 7. Documentation & Verification

- [ ] 7.1 Run full backend build and tests: `./mvnw clean package`
- [ ] 7.2 Run full frontend build and tests: `pnpm build && pnpm test`
- [ ] 7.3 Manual verification: Create profile with default rate (should be 1.00)
- [ ] 7.4 Manual verification: Edit profile to set rate to valid value (e.g., 50.00)
- [ ] 7.5 Manual verification: Attempt invalid rate (0.50) - should show error
- [ ] 7.6 Manual verification: Enter rate > 200 - should show warning modal
- [ ] 7.7 Verify Swagger UI shows hourlyRate in profile schema
