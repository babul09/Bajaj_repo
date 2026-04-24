# Quiz Validator Integration Service

Java 21 + Spring Boot service that integrates with the validator quiz API, polls exactly 10 message batches, deduplicates repeated events with the required `(roundId + participant)` key, computes participant totals, submits the final leaderboard once, and exposes the full audit trail through an API and a simple built-in HTML page.

## What This Project Does

The validator behaves like a quiz show backend. Each poll returns a batch of participant scores for rounds, and the same event can reappear across later polls. The service solves that by:

1. Polling `GET /quiz/messages` for `poll=0..9`
2. Enforcing a minimum 5-second gap between poll starts
3. Retrying transient failures with a bounded retry policy
4. Deduplicating events by `(roundId + participant)`
5. Aggregating scores into `totalScore` per participant
6. Sorting the leaderboard in descending score order
7. Computing the combined submitted total
8. Submitting the leaderboard exactly once to `POST /quiz/submit`
9. Persisting the scoring and submission outcome in the run status response

## Tech Stack

- Java 21
- Spring Boot 3
- Maven
- JUnit 5

## Project Structure

- `src/main/java/com/bajaj/quizvalidator/api`
  REST endpoints for starting a run and reading current status.
- `src/main/java/com/bajaj/quizvalidator/application`
  Run orchestration, polling timing, retry policy, scoring and submission snapshots.
- `src/main/java/com/bajaj/quizvalidator/domain/scoring`
  Pure scoring logic for deduplication, aggregation, and leaderboard sorting.
- `src/main/java/com/bajaj/quizvalidator/integration`
  Validator client abstraction and HTTP implementation.
- `src/main/java/com/bajaj/quizvalidator/integration/dto`
  DTOs for poll and submit contracts.
- `src/main/resources/static/index.html`
  Basic browser page for starting a run and viewing the leaderboard.

## Prerequisites

- Java 21 installed
- Maven installed
- Internet access to reach:
  `https://devapigw.vidalhealthtpa.com/srm-quiz-task`

## Configuration

The default configuration lives in `src/main/resources/application.yml`.

```yaml
server:
  port: 8080

validator:
  base-url: https://devapigw.vidalhealthtpa.com/srm-quiz-task
  connect-timeout-ms: 5000
  read-timeout-ms: 10000
  min-poll-interval-ms: 5000
  retry-max-attempts: 3
```

Key properties:

- `validator.base-url`
  Base URL for the external validator service.
- `validator.min-poll-interval-ms`
  Required delay between consecutive poll starts.
- `validator.retry-max-attempts`
  Maximum attempts for retryable poll failures.

## Running The Service

Start the application:

```bash
mvn spring-boot:run
```

Once started:

- API base: `http://localhost:8080`
- HTML page: `http://localhost:8080/`

## Browser UI

The app now includes a simple HTML page at `/` that lets you:

- enter a `regNo`
- start a run
- refresh live status
- view the final leaderboard
- inspect scoring and submission audit details

The page uses the backend API directly, so there is no separate frontend build step.

## API Endpoints

### `POST /api/runs`

Starts an asynchronous run.

Request:

```json
{
  "regNo": "2024CS101"
}
```

Success response:

```json
{
  "runId": "generated-run-id",
  "message": "Run started"
}
```

If another run is already active, the service returns HTTP `409 Conflict`.

### `GET /api/runs/status`

Returns the current run snapshot, including:

- run lifecycle data
- poll progress
- retry count
- scoring audit fields
- leaderboard
- submission audit fields
- final run summary

Typical fields in the response:

- `state`
- `currentPollIndex`
- `retryCount`
- `uniqueEventCount`
- `duplicateEventCount`
- `participantCount`
- `combinedTotalScore`
- `leaderboard`
- `submissionAttempted`
- `duplicateSubmissionBlocked`
- `submittedTotal`
- `expectedTotal`
- `isCorrect`
- `isIdempotent`
- `submitMessage`
- `runSummary`

## Example Usage With cURL

Start a run:

```bash
curl -X POST http://localhost:8080/api/runs \
  -H "Content-Type: application/json" \
  -d '{"regNo":"2024CS101"}'
```

Check status:

```bash
curl http://localhost:8080/api/runs/status
```

## Validator Contract Coverage

### Polling

- Executes exactly 10 polls using `poll=0..9`
- Enforces at least 5 seconds between consecutive poll starts
- Logs each poll attempt with `poll_index`, `attempt`, and `outcome`
- Retries transient failures up to the configured max attempts

### Deduplication And Scoring

- Uses `(roundId + participant)` as the only deduplication key
- Aggregates only deduplicated events
- Sorts leaderboard by descending `totalScore`
- Computes the combined total from the final leaderboard

### Submission

- Sends exactly one validator submission per run
- Uses payload shape:

```json
{
  "regNo": "2024CS101",
  "leaderboard": [
    { "participant": "Alice", "totalScore": 100 },
    { "participant": "Bob", "totalScore": 120 }
  ]
}
```

- Captures validator response fields such as:
  `isCorrect`, `isIdempotent`, `submittedTotal`, `expectedTotal`, and `message`
- Blocks duplicate submission attempts inside the same run

## Observability

The service exposes evaluator-friendly visibility in two ways:

- structured logs for each poll and final scoring/submission summaries
- `/api/runs/status` for a machine-readable snapshot of the current or completed run

The final run summary includes:

- unique events processed
- duplicates dropped
- participant totals
- leaderboard values
- submitted total
- validator submission outcome

## Testing

Run all tests with:

```bash
mvn test
```

Current automated coverage includes:

- exact poll sequence validation for `0..9`
- minimum delay enforcement
- bounded retry behavior
- scoring after all polls complete
- duplicate submission guard behavior

## Notes

- The validator endpoint is external, so live runs depend on network availability.
- The poll contract is strict: use only `poll=0` through `poll=9`.
- The service allows repeated manual runs over time, but only one active run at once.

## Quick Demo Flow

1. Start the service with `mvn spring-boot:run`
2. Open `http://localhost:8080/`
3. Enter a registration number such as `2024CS101`
4. Click `Start Run`
5. Wait for the run to complete while the page refreshes status
6. Review the leaderboard, totals, and validator response fields
