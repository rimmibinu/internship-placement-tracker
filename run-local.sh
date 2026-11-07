#!/usr/bin/env bash
# Starts eligibility service (8081), backend (8080) and frontend (5173) for local development.
# Prerequisites: JDK 17+, Maven 3.9+, Node 18+.
set -e
cd "$(dirname "$0")"
(cd eligibility-service && mvn -q spring-boot:run) &
(cd backend && mvn -q spring-boot:run) &
(cd frontend && npm install && npm run dev) &
trap 'kill 0' EXIT
wait
