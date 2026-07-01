# Auth Service Testing Resources

This directory contains resources for testing the auth-service endpoints.

## Overview

The auth-service provides the following endpoints:

1. `POST /auth/register` - Register a new user
2. `POST /auth/login` - Authenticate a user and get a JWT token
3. `GET /auth/me` - Get the current authenticated user's information

## Testing Resources

### 1. Sample Payloads

The file `payloads.md` contains sample request and response payloads for all endpoints, including success and error scenarios. Use these as a reference when testing the API manually or when creating automated tests.

### 2. Postman Collection

The file `Auth_Service.postman_collection.json` is a Postman collection that can be imported into Postman for easier testing. It includes:

- Requests for all endpoints
- Sample request payloads
- Tests for success and error scenarios
- Automatic token extraction and reuse

To use the Postman collection:

1. Import the collection into Postman
2. Create an environment with a variable `base_url` set to `http://localhost:8084` (or your auth-service URL)
3. Run the requests in sequence (register, login, get current user)

### 3. JUnit Tests

The class `com.org.authservice.controller.AuthControllerTest` contains JUnit tests for all endpoints. These tests demonstrate how to:

- Create test users in the database
- Send requests to the auth-service endpoints
- Verify the responses
- Extract and use JWT tokens
- Test error scenarios

To run the JUnit tests:

```bash
./gradlew test --tests "com.org.authservice.controller.AuthControllerTest"
```

## Testing Flow

A typical testing flow would be:

1. Register a new user (`POST /auth/register`)
2. Login with the registered user credentials (`POST /auth/login`)
3. Use the token from the login response to access the me endpoint (`GET /auth/me`)

## Error Scenarios

The testing resources cover the following error scenarios:

- Registration with an email that already exists
- Registration with an invalid email format
- Registration with a password that's too short
- Login with incorrect credentials
- Login with a non-existent user
- Accessing protected endpoints without a token
- Accessing protected endpoints with an invalid token
- Accessing protected endpoints with an expired token

## Manual Testing with Curl

You can also test the auth-service manually using curl commands:

```bash
# Register a new user
curl -X POST http://localhost:8084/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "secure123"}'

# Login
curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "secure123"}'

# Get current user info
curl -X GET http://localhost:8084/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

Replace `YOUR_TOKEN_HERE` with the token received from the login response.