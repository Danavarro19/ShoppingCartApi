# Auth Service Test Payloads

This document contains sample payloads for testing the auth-service endpoints.

## 1. Register Endpoint

**Endpoint:** `POST /auth/register`

### Success Scenario

**Request:**
```json
{
  "email": "user@example.com",
  "password": "secure123"
}
```

**Response:** (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTYzMDAwMDAwMCwiZXhwIjoxNjMwMDg2NDAwfQ.signature"
}
```

### Error Scenarios

#### Email Already Registered

**Request:**
```json
{
  "email": "existing@example.com",
  "password": "secure123"
}
```

**Response:** (400 Bad Request)
```json
{
  "error": "Email already registered"
}
```

#### Invalid Email Format

**Request:**
```json
{
  "email": "invalid-email",
  "password": "secure123"
}
```

**Response:** (400 Bad Request)
```json
{
  "email": "Email should be valid"
}
```

#### Missing Email

**Request:**
```json
{
  "password": "secure123"
}
```

**Response:** (400 Bad Request)
```json
{
  "email": "Email is required"
}
```

#### Password Too Short

**Request:**
```json
{
  "email": "user@example.com",
  "password": "short"
}
```

**Response:** (400 Bad Request)
```json
{
  "password": "Password must be at least 6 characters"
}
```

#### Missing Password

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:** (400 Bad Request)
```json
{
  "password": "Password is required"
}
```

## 2. Login Endpoint

**Endpoint:** `POST /auth/login`

### Success Scenario

**Request:**
```json
{
  "email": "user@example.com",
  "password": "secure123"
}
```

**Response:** (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTYzMDAwMDAwMCwiZXhwIjoxNjMwMDg2NDAwfQ.signature"
}
```

### Error Scenarios

#### Invalid Credentials

**Request:**
```json
{
  "email": "user@example.com",
  "password": "wrongpassword"
}
```

**Response:** (401 Unauthorized)
```json
{
  "error": "Invalid email or password"
}
```

#### User Not Found

**Request:**
```json
{
  "email": "nonexistent@example.com",
  "password": "secure123"
}
```

**Response:** (401 Unauthorized)
```json
{
  "error": "Invalid email or password"
}
```

#### Invalid Email Format

**Request:**
```json
{
  "email": "invalid-email",
  "password": "secure123"
}
```

**Response:** (400 Bad Request)
```json
{
  "email": "Email should be valid"
}
```

#### Missing Email

**Request:**
```json
{
  "password": "secure123"
}
```

**Response:** (400 Bad Request)
```json
{
  "email": "Email is required"
}
```

#### Missing Password

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:** (400 Bad Request)
```json
{
  "password": "Password is required"
}
```

## 3. Me Endpoint

**Endpoint:** `GET /auth/me`

### Success Scenario

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTYzMDAwMDAwMCwiZXhwIjoxNjMwMDg2NDAwfQ.signature
```

**Response:** (200 OK)
```json
{
  "id": 1,
  "email": "user@example.com",
  "role": "USER"
}
```

### Error Scenarios

#### Missing Token

**Request Headers:**
```
(No Authorization header)
```

**Response:** (401 Unauthorized)
```
No response body, redirects to login
```

#### Invalid Token

**Request Headers:**
```
Authorization: Bearer invalid.token.here
```

**Response:** (401 Unauthorized)
```
No response body, redirects to login
```

#### Expired Token

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTYzMDAwMDAwMCwiZXhwIjoxNjMwMDAwMDAxfQ.signature
```

**Response:** (401 Unauthorized)
```
No response body, redirects to login
```

## 4. Testing Flow

A typical testing flow would be:

1. Register a new user (`POST /auth/register`)
2. Login with the registered user credentials (`POST /auth/login`)
3. Use the token from the login response to access the me endpoint (`GET /auth/me`)

## 5. Curl Commands for Testing

### Register a new user
```bash
curl -X POST http://localhost:8084/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "secure123"}'
```

### Login
```bash
curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "secure123"}'
```

### Get current user info
```bash
curl -X GET http://localhost:8084/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```