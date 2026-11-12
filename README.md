# Budget App

A simple web app that helps you budget your month and keep track of what you spend money on.

3rd semester Computer Science project, built in IntelliJ with Java (Spring Boot), HTML and JavaScript.

## Features
- Add expenses manually (rent, insurance, food ...) and mark fixed monthly costs
- Automatic categorization of expenses by keywords
- Upload a picture of a receipt: the text is read with the **OCR.space API** and the form is filled in
- Estimate of next month's spending (fixed costs + average variable spending)
- Advice based on your income and spending per category

## Tech
- Java 21, Spring Boot (Web + JPA)
- H2 database (saved in a file)
- HTML, CSS and JavaScript (fetch)
- OCR.space API (API key)

## Run locally
1. Get a free API key at https://ocr.space/ocrapi
2. Put it in `src/main/resources/application.properties` (`ocr.api.key=...`) or set the environment variable `OCR_API_KEY`
3. Run `BudgetApplication.java` in IntelliJ
4. Open http://localhost:8080

## Run online
The project includes a `Dockerfile`, so it can be deployed on e.g. Render.com. Set `OCR_API_KEY` as an environment variable there.
