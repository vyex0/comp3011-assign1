# COMP3011 Assignment 1 - Java Web Application
A Spring Boot web app that records audio in the browser, sends it to OpenAI's gpt-4o-mini-transcribe API, and displays the returned text transcription.

# Application Structure Notes
- Concurrency approach: `spring.threads.virtual.enabled=true` means each incoming request runs on a JDK virtual thread instead of a platform thread. Most of the work for each request is spent waiting for the OpenAI transcription response, so the application is mainly I/O-bound rather than CPU-bound. With virtual threads, this waiting does not tie up a platform thread, allowing the application to handle hundreds of /stt requests at the same time while still using normal blocking code through RestClient. ConcurrencyLoadTest demonstrates that this approach works under load.
- **API key handling:** `OPENAI_API_KEY` is read once from the environment at startup (`OpenAIConfig`) and attached as a default header on the shared `RestClient` bean. It is never logged, never returned in a response, and never hardcoded.

# Testing Strategy
## AdminControllerTest
Covers all three YAML-specified endpoints and their documented status codes (200, 202, 409, and 500). It also checks that every error response contains the expected fields: status, error, timestamp, and path. This means issues like accidentally renaming a field will be caught during testing instead of being discovered during grading.

## SttServiceTest
Uses MockRestServiceServer to mock the OpenAI API call, so the tests don't rely on an internet connection or use the real API. It tests three cases:
- Successful transcription — checks that the returned text and token counts are parsed correctly, and that TokenStatsService is updated with the right values.
- OpenAI call fails — checks that the exception is passed on and, importantly, that TokenStatsService is not updated when the request fails, preventing incorrect or duplicate token counts.
- Logging — captures the SttService logger output and checks that the token counts are actually logged, rather than just calculated internally.

## SttControllerTest
Checks that the controller correctly calls SttService and returns the response in the expected format. SttService is mocked for this test, which keeps the focus on the controller's routing and response serialization rather than the transcription logic, which is tested separately.

## TokenStatsServiceConcurrencyTest
Creates 500 virtual threads that each increment the same counter 100 times, then checks that the final total is exactly 50,000. If AtomicLong were replaced with a normal long, some updates could be lost when multiple threads access it at the same time. This test therefore helps catch the race condition that the rubric is looking for.

## ConcurrencyLoadTest
Sends 500 concurrent HTTP requests to a running instance of the application using TestRestTemplate and a random port. SttService is mocked with a fixed 300ms delay to simulate the time taken by OpenAI. The test checks that all 500 requests return 200 and that they all finish within 1500ms. If the requests were processed one at a time, they would take around 150 seconds, so completing them within 1.5 seconds demonstrates that the application is handling the requests concurrently rather than queuing them.