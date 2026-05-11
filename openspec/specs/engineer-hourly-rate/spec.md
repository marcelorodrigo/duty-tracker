# Engineer Hourly Rate

## Purpose
Enable engineers to configure and manage their hourly rate, which is used for earnings calculations and compensation tracking.

## Requirements

### Requirement: Engineer can set hourly rate
Engineers can set and update their hourly rate in their profile. The hourly rate is used to calculate compensation in future earnings reports.

#### Scenario: Engineer sets valid hourly rate
- **WHEN** an engineer edits their profile and sets hourlyRate to 50.00
- **THEN** the system saves the rate and displays it in their profile

#### Scenario: Engineer attempts rate less than or equal to 1.00
- **WHEN** an engineer submits hourlyRate = 1.00
- **THEN** the system rejects with 400 Bad Request and message "Hourly rate must be greater than 1"

#### Scenario: Frontend warns on unusual rate
- **WHEN** an engineer enters hourlyRate > 200 in the profile form
- **THEN** a warning displays: "Unusual hourly rate. Continue?" and allows them to proceed or cancel

### Requirement: Default hourly rate is unconfigured sentinel
New engineer profiles initialize with hourlyRate = 1.00 to indicate the rate has not been configured.

#### Scenario: New profile gets default rate
- **WHEN** an engineer profile is created
- **THEN** hourlyRate is set to 1.00

#### Scenario: Reports detect unconfigured rate
- **WHEN** a future earnings report generates for an engineer with hourlyRate = 1.00
- **THEN** an alert appears: "Hourly rate not configured. Earnings calculation may be inaccurate. Go to profile settings."
