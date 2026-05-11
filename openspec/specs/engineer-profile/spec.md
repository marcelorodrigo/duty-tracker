# Engineer Profile

## Purpose
Define engineer profile capabilities including profile updates with support for configurable settings like working days, work hours, and hourly rate.

## Requirements

### Requirement: Engineer can update profile settings
Engineers can update their profile settings including working days, work hours, and hourly rate.

#### Scenario: Update profile with valid hourly rate
- **WHEN** an engineer submits a profile update with hourlyRate = 75.50
- **THEN** the system validates the rate is > 1.00, saves all updated fields, and returns success

#### Scenario: Update profile with invalid hourly rate
- **WHEN** an engineer submits a profile update with hourlyRate = 0.50
- **THEN** the system returns 400 Bad Request with message "Hourly rate must be greater than 1"

#### Scenario: Profile preserves hourly rate on other updates
- **WHEN** an engineer updates working days without changing hourlyRate
- **THEN** the system saves the working days and preserves the existing hourlyRate value
