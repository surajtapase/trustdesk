## TrustDesk – AI Support Operations Agent

### Overview

TrustDesk is an AI-first customer support operations platform built with Spring Boot and React.

It provides operators with AI-assisted ticket triage, grounded support responses, knowledge-base citations, guardrails, approval-gated actions, idempotency, and AI audit traces.

### Key Features

* AI ticket triage
* Intent and priority classification
* Knowledge-base retrieval
* Grounded customer support responses with citations
* Prompt-injection and sensitive-information guardrails
* Human approval workflow for sensitive actions
* Idempotent refund-review action
* AI audit traces
* Mock/provider-independent AI architecture
* React operator dashboard
* PostgreSQL persistence
* Automated evaluation suite

### Validation

* Evaluation cases: **8/8 PASS**
* Automated backend tests: **9/9 PASS**
* Backend: Spring Boot + Java 21
* Frontend: React + Vite
* Database: PostgreSQL

### Repository Structure

```text
trustdesk/
├── src/              # Spring Boot backend
├── data/             # Application and evaluation data
├── frontend/         # React frontend
├── pom.xml
├── README.md
└── .gitignore
```

### Repository

https://github.com/surajtapase/trustdesk
