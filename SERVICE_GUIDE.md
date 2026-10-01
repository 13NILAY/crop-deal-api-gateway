# API Gateway Guide

## Purpose
API Gateway is the single HTTP entry point for clients. Angular or Postman should call the Gateway instead of calling every microservice directly.

## Runtime
- Port: `8080`
- Application name: `api-gateway`
- Discovery: Eureka
- Security: shared-secret JWT validation

## Routing
- `/auth/**` -> Authentication Service
- `/farmers/**` -> Profile Service
- `/crops/**` -> Crop Service
- `/dealers/**` -> Profile Service
- `/orders/**` -> Order Service
- `/payments/**` -> Payment Service
- `/bank-accounts/**` -> Payment Service
- `/payouts/**` -> Payment Service
- `/notifications/**` -> Notification Service

The `lb://SERVICE-NAME` URI means Gateway asks Eureka for a healthy service instance and load-balances the request.

## Request flow
1. Client sends a request to `http://localhost:8080`.
2. Gateway checks the bearer JWT when the route is protected.
3. Gateway discovers the destination through Eureka.
4. The request is forwarded to the destination service.
5. The destination service performs its own authorization and business validation.

Gateway security is not a replacement for service security. Every business service validates the token again.

## Public routes
Authentication registration/login and appropriate crop read endpoints are public. Profile, order, payment, payout, bank-account, and notification operations require JWT authentication.
