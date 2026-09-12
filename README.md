# COMP3011 Assignment 1 - Java Web Application
Application records your audio, calls the OpenAI API to transcribe the speech into text, and returns the text into the HTML page.

# Testing Strategy
## AdminControllerTest
Covers all three endpoints and status codes defined in the YAML spec (200/202/409/500), using the full ErrorResponse shape (status, error, timestamp, path) on failure paths so a schema mismatch (e.g. wrong field name) would be caught here.

## SttServiceTest
Uses MockRestServiceServer to stub the OpenAI call entirely. Three cases: successful transcription updates both the parsed response and TokenStatsService correctly, 

## TokenStatsServiceConcurrencyTest
Forces 500 threads to concurrently increment the same counter 100 times each, then asserts the total is mathematically exact. This simulates a race-condition test, if AtomicLong were swapped for a plain long, this test would fail from lost updates.

## ConcurrencyLoadTest
Simulates 500 concurrent clients hitting /stt at once, using virtual threads for the client-side load generator 