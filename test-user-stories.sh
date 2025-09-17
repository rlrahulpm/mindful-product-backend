#!/bin/bash

# Script to test user story endpoints

# Configuration
BASE_URL="http://localhost:8080/api/v3"
PRODUCT_ID="1"
EPIC_ID="test-epic-123"
TOKEN="your-jwt-token-here"

echo "Testing User Stories API..."
echo "=========================="

# Test 1: Create a user story
echo "1. Creating a user story..."
curl -X POST "${BASE_URL}/products/${PRODUCT_ID}/epics/${EPIC_ID}/user-stories" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "As a user, I want to login",
    "description": "Enable user authentication",
    "acceptanceCriteria": "User can login with email and password",
    "priority": "High",
    "storyPoints": 5
  }'

echo -e "\n"

# Test 2: Get all stories for an epic
echo "2. Getting all stories for epic ${EPIC_ID}..."
curl -X GET "${BASE_URL}/products/${PRODUCT_ID}/epics/${EPIC_ID}/user-stories" \
  -H "Authorization: Bearer ${TOKEN}"

echo -e "\n"

# Test 3: Get all stories for product
echo "3. Getting all stories for product ${PRODUCT_ID}..."
curl -X GET "${BASE_URL}/products/${PRODUCT_ID}/user-stories" \
  -H "Authorization: Bearer ${TOKEN}"

echo -e "\n\nNote: Replace 'your-jwt-token-here' with an actual JWT token from your login."