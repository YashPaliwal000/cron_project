#!/bin/bash

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Get current time and future time
CURRENT_TIME=$(date -u '+%Y-%m-%dT%H:%M:%SZ')
FUTURE_TIME=$(date -u -d "+5 minutes" '+%Y-%m-%dT%H:%M:%SZ')

echo -e "${BLUE}========== Job Scheduler Test ==========${NC}\n"
echo -e "Current Time: ${YELLOW}${CURRENT_TIME}${NC}"
echo -e "Job Schedule: ${YELLOW}${FUTURE_TIME}${NC} (5 minutes from now)\n"

# Check if server is running
echo -e "${BLUE}Checking if server is running...${NC}"
if curl -s http://localhost:8080/api/jobs > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is running${NC}\n"
else
    echo -e "${RED}✗ Server is not running${NC}"
    echo "Start the server with: ./mvnw spring-boot:run"
    exit 1
fi

# Create the job
echo -e "${BLUE}Creating test job...${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Test Job - 5 Minutes\",
    \"apiEndpoint\": \"https://jsonplaceholder.typicode.com/posts/1\",
    \"method\": \"GET\",
    \"headers\": {
      \"Accept\": \"application/json\"
    },
    \"scheduleTime\": \"${FUTURE_TIME}\"
  }")

# Extract job ID
JOB_ID=$(echo $RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)

if [ -z "$JOB_ID" ]; then
    echo -e "${RED}✗ Failed to create job${NC}"
    echo "Response: $RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Job created successfully${NC}"
echo -e "Job ID: ${YELLOW}${JOB_ID}${NC}\n"

# Display next steps
echo -e "${BLUE}Next Steps:${NC}"
echo "1. Wait 5 minutes for the job to execute"
echo "2. Check the server logs for execution details"
echo "3. Check execution history with:"
echo ""
echo -e "   ${YELLOW}curl http://localhost:8080/api/jobs/${JOB_ID}/executions${NC}"
echo ""
echo "4. Or query the database:"
echo ""
echo -e "   ${YELLOW}psql -U postgres -h localhost -d job_scheduler${NC}"
echo ""
echo -e "   ${YELLOW}SELECT * FROM job_execution WHERE job_id = '${JOB_ID}';${NC}"
echo ""

# Optional: Save to environment
echo -e "${BLUE}Saved Job ID for reference: ${NC}"
echo "export TEST_JOB_ID=${JOB_ID}"
echo ""
echo -e "${YELLOW}Set the environment variable with:${NC}"
echo -e "   ${GREEN}export TEST_JOB_ID=${JOB_ID}${NC}"

