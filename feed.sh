#!/bin/bash

# feed.sh - Seed on-call periods and incidents for duty-tracker
# Creates 3 on-call periods with realistic incidents, including holidays and override holidays

set -e

# Configuration
API_BASE_URL="${API_URL:-http://localhost:8080}"
API_ENDPOINT="$API_BASE_URL/api/v1"
MAX_RETRIES=30
RETRY_DELAY=2

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
  echo -e "${BLUE}ℹ ${NC}$1" >&2
}

log_success() {
  echo -e "${GREEN}✓ ${NC}$1" >&2
}

log_error() {
  echo -e "${RED}✗ ${NC}$1" >&2
}

log_section() {
  echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}" >&2
  echo -e "${YELLOW}  $1${NC}" >&2
  echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n" >&2
}

# Simple date addition for bash (YYYY-MM-DD format)
add_days() {
  local date="$1"
  local days="$2"
  
  # Convert date to seconds since epoch
  date -jf "%Y-%m-%d" -v+${days}d "$date" +%Y-%m-%d 2>/dev/null || \
    date -d "$date + $days days" +%Y-%m-%d 2>/dev/null || \
    python3 -c "from datetime import datetime, timedelta; print((datetime.strptime('$date', '%Y-%m-%d') + timedelta(days=$days)).strftime('%Y-%m-%d'))"
}

# Get day of week (1=Monday, 7=Sunday)
get_dow() {
  local date="$1"
  date -jf "%Y-%m-%d" "$date" +%u 2>/dev/null || \
    date -d "$date" +%u 2>/dev/null || \
    python3 -c "from datetime import datetime; print(datetime.strptime('$date', '%Y-%m-%d').isoweekday())"
}

# Subtract days from a date
subtract_days() {
  local date="$1"
  local days="$2"
  add_days "$date" "-$days"
}

# Check backend is running
wait_for_backend() {
  log_info "Waiting for backend to be ready at $API_ENDPOINT..."
  
  local attempt=1
  while [ $attempt -le $MAX_RETRIES ]; do
    if curl -s "$API_ENDPOINT/oncall-periods" >/dev/null 2>&1; then
      log_success "Backend is ready!"
      return 0
    fi
    
    echo -ne "${BLUE}ℹ ${NC}Attempt $attempt/$MAX_RETRIES (waiting ${RETRY_DELAY}s)...\r" >&2
    sleep $RETRY_DELAY
    ((attempt++))
  done
  
  log_error "Backend is not responding after $MAX_RETRIES attempts"
  exit 1
}

# Clear existing on-call periods
clear_data() {
  log_info "Fetching existing on-call periods..."
  
  local periods=$(curl -s "$API_ENDPOINT/oncall-periods" | jq -r '.periods[].id' 2>/dev/null || echo "")
  
  if [ -z "$periods" ]; then
    log_success "No existing on-call periods to clear"
    return 0
  fi
  
  log_info "Deleting existing on-call periods..."
  while read -r period_id; do
    if [ -n "$period_id" ]; then
      log_info "  Deleting period ID: $period_id"
      curl -s -X DELETE "$API_ENDPOINT/oncall-periods/$period_id" >/dev/null
    fi
  done <<< "$periods"
  
  log_success "All existing on-call periods cleared"
}

# Create on-call period
create_period() {
  local start_dt="$1"
  local end_dt="$2"
  local name="$3"
  
  log_info "Creating on-call period: $name"
  log_info "  Start: $start_dt"
  log_info "  End: $end_dt"
  
  local response=$(curl -s -X POST "$API_ENDPOINT/oncall-periods" \
    -H "Content-Type: application/json" \
    -d "{\"startDateTime\": \"$start_dt\", \"endDateTime\": \"$end_dt\"}")
  
  local period_id=$(echo "$response" | jq -r '.id // empty' 2>/dev/null)
  
  if [ -z "$period_id" ]; then
    log_error "Failed to create on-call period: $name"
    log_error "Response: $response"
    exit 1
  fi
  
  log_success "Created period ID: $period_id"
  echo "$period_id"
}

# Add holiday override
add_holiday_override() {
  local period_id="$1"
  local date="$2"
  
  log_info "Adding holiday override for $date on period $period_id"
  
  local response=$(curl -s -X POST "$API_ENDPOINT/oncall-periods/$period_id/holidays" \
    -H "Content-Type: application/json" \
    -d "{\"date\": \"$date\"}")
  
  local status=$(echo "$response" | jq -r '.id // empty' 2>/dev/null)
  
  if [ -z "$status" ]; then
    log_error "Failed to add holiday override for $date"
    log_error "Response: $response"
    exit 1
  fi
  
  log_success "Holiday override added for $date"
}

# Create incident
create_incident() {
  local period_id="$1"
  local name="$2"
  local date="$3"
  local start_time="$4"
  local end_time="$5"
  
  log_info "Creating incident: $name ($date $start_time - $end_time)"
  
  local response=$(curl -s -X POST "$API_ENDPOINT/incidents" \
    -H "Content-Type: application/json" \
    -d "{\"onCallPeriodId\": $period_id, \"name\": \"$name\", \"date\": \"$date\", \"startTime\": \"$start_time\", \"endTime\": \"$end_time\"}")
  
  local incident_id=$(echo "$response" | jq -r '.id // empty' 2>/dev/null)
  
  if [ -z "$incident_id" ]; then
    log_error "Failed to create incident: $name"
    log_error "Response: $response"
    exit 1
  fi
  
  log_success "Created incident ID: $incident_id - $name"
  echo "$incident_id"
}

# Main execution
main() {
  log_section "Duty Tracker - On-Call Period Seeder"
  
  # Step 1: Check backend
  wait_for_backend
  
  # Step 2: Clear existing data
  log_section "Clearing Existing Data"
  clear_data
  
  # Step 3: Calculate dates relative to today
  log_section "Setting Up On-Call Periods"
  
  # Get today's date and day of week
  local today=$(date +%Y-%m-%d)
  local today_dow=$(get_dow "$today")
  
  # Calculate days until Monday (past or current)
  local days_to_past_monday=$((today_dow - 1))
  
  # Calculate Monday dates (3 Mondays and a gap week)
  local monday_2w_ago=$(subtract_days "$today" $((days_to_past_monday + 14)))
  local monday_1w_ago=$(subtract_days "$today" $((days_to_past_monday + 7)))
  local monday_this_week=$(subtract_days "$today" $days_to_past_monday)
  local monday_next_week=$(add_days "$today" $((7 - days_to_past_monday)))
  
  log_info "Today: $today (day of week: $today_dow)"
  log_info "Monday 2 weeks ago: $monday_2w_ago"
  log_info "Monday 1 week ago: $monday_1w_ago"
  log_info "Monday this week: $monday_this_week"
  log_info "Monday next week: $monday_next_week"
  
  echo "" >&2
  
  # Period 1: 2 weeks ago Monday 14:00 to 1 week ago Monday 14:00 (no incidents)
  local p1_start="${monday_2w_ago}T14:00:00"
  local p1_end="${monday_1w_ago}T14:00:00"
  local period_1=$(create_period "$p1_start" "$p1_end" "Period 1 (2 weeks ago)")
  
  log_success "Period 1 complete with 0 incidents (clean baseline)"
  
  echo "" >&2
  
  # Period 2: 1 week ago Monday 14:00 to this week Monday 14:00 (2 incidents)
  local p2_start="${monday_1w_ago}T14:00:00"
  local p2_end="${monday_this_week}T14:00:00"
  local period_2=$(create_period "$p2_start" "$p2_end" "Period 2 (last week)")
  
  # Calculate incident dates for Period 2
  local p2_wed=$(add_days "$monday_1w_ago" 2)
  local p2_fri=$(add_days "$monday_1w_ago" 4)
  
  # Period 2 incidents (off-hours with compensation)
  create_incident "$period_2" "Database Backup Failure" "$p2_wed" "22:00" "23:30"
  create_incident "$period_2" "API Server Crash" "$p2_fri" "02:00" "03:15"
  
  log_success "Period 2 complete with 2 incidents"
  
  echo "" >&2
  
  # Period 3: This week Monday 14:00 to next Monday 14:00 (3 incidents)
  local p3_start="${monday_this_week}T14:00:00"
  local p3_end="${monday_next_week}T14:00:00"
  local period_3=$(create_period "$p3_start" "$p3_end" "Period 3 (CURRENT/ACTIVE)")
  
  # Calculate incident dates for Period 3
  local p3_tue=$(add_days "$monday_this_week" 1)
  local p3_fri=$(add_days "$monday_this_week" 4)
  local p3_sun=$(add_days "$monday_this_week" 6)
  
  # Period 3 incidents
  create_incident "$period_3" "Memory Leak Investigation" "$p3_tue" "23:00" "00:45"
  
  # Thursday of this week (today-1, off-hours with compensation)
  local p3_thu=$(add_days "$monday_this_week" 3)
  create_incident "$period_3" "Critical Security Patch Deployment" "$p3_thu" "03:00" "04:30"
  
  # Wednesday of this week - add a holiday override and create incident on that day
  local p3_wed=$(add_days "$monday_this_week" 2)
  add_holiday_override "$period_3" "$p3_wed"
  create_incident "$period_3" "Incident on Override Holiday" "$p3_wed" "19:00" "20:15"
  
  log_success "Period 3 complete with 3 incidents (includes holiday and override holiday)"
  
  # Summary
  log_section "Seeding Complete"
  echo -e "
${GREEN}Summary:${NC}
  Period 1 (ID: $period_1): $monday_2w_ago to $monday_1w_ago → 0 incidents
  Period 2 (ID: $period_2): $monday_1w_ago to $monday_this_week → 2 incidents
  Period 3 (ID: $period_3): $monday_this_week to $monday_next_week → 3 incidents (ACTIVE)
    - Includes 1 incident on Wednesday (override holiday with 0% compensation)
    - Includes 1 incident on Thursday (off-hours with 50% compensation)

${YELLOW}Next steps:${NC}
  1. Visit http://localhost:3000 to view the frontend
  2. Check the Swagger UI at http://localhost:8080/swagger-ui.html
  3. Run calculations: POST /api/v1/oncall-periods/{id}/calculate
" >&2
}

main "$@"
