# Budget Tracker

An AI-based web app that helps you budget your month and keep track of what you spend money on.

3rd semester Computer Science project, built in IntelliJ with Java (Spring Boot), HTML, CSS and JavaScript.

## Features
- Enter this month's income and add expenses manually (rent, insurance, food ...)
- Mark fixed monthly costs like rent and insurance
- Automatic categorization by keywords (Housing, Insurance, Food, Transport, Subscriptions, Fun, Other)
- Upload a picture of a receipt: **OCR.space** (AI text recognition) reads it, and the app finds the store, total, category and date
- Estimate of next month's spending (fixed costs + average variable spending per month)
- Personal budget advice from the AI model **Claude** (Anthropic API), based on your income and spending
- Overview with key numbers, spending per category and a list of all expenses

## How it works
1. The browser (`script.js`) talks to the Java backend through REST endpoints under `/api`.
2. `ExpenseController` saves and reads expenses in the H2 database with Spring Data JPA.
3. `OcrService` sends receipt pictures to the OCR.space API and finds the total and date with regex.
4. `AiService` sends the budget numbers to the Claude API and gets advice back.

| Endpoint | What it does |
|---|---|
| `GET /api/expenses` | All expenses |
| `POST /api/expenses` | Add an expense |
| `DELETE /api/expenses/{id}` | Delete an expense |
| `POST /api/receipt` | Read a receipt picture |
| `GET /api/summary?income=...` | Spending per category, estimate and advice |

## Tech
- Java 21, Spring Boot (Web + JPA), Maven
- H2 database (saved in a file)
- HTML, CSS and JavaScript (fetch)
- OCR.space API (API key)
- Anthropic Claude API, model Claude Haiku (API key)
- Docker (for running online)

## API keys
The keys are read from environment variables, so they are never in the code or on GitHub.

| Variable | Where to get it |
|---|---|
| `OCR_API_KEY` | Free key at https://ocr.space/ocrapi |
| `ANTHROPIC_API_KEY` | https://console.anthropic.com (API Keys) |

Without `ANTHROPIC_API_KEY` the app still works and shows simple rule-based advice instead.

## Run locally
1. Get the two API keys above.
2. In IntelliJ: **Run → Edit Configurations → Environment variables**:
   `OCR_API_KEY=your-ocr-key;ANTHROPIC_API_KEY=your-claude-key`
3. Run `BudgetApplication.java`.
4. Open http://localhost:8080

## Run online
The project includes a `Dockerfile`, so it can be deployed on e.g. Render.com.
Set `OCR_API_KEY` and `ANTHROPIC_API_KEY` as environment variables there.
