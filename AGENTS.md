# Agent Instructions

This project is a hands-on Java automated testing learning project. The user is learning through code, tests, and documentation. Keep the learning experience incremental, practical, and documented.

## Language

Use Chinese when explaining concepts, giving learning tasks, reviewing tests, or writing learning documentation.

Use English only where it is natural for code, identifiers, commands, dependency names, or framework terminology.

## Project Context

Project root:

```text
/Users/yezi/IdeaProjects/java-testing-lab
```

This is a Maven Java 17 project focused on automated testing.

Core learning documents:

```text
README.md
docs/learning-plan.md
docs/learning-method.md
docs/session-progress.md
docs/lesson-01-junit5.md
docs/lesson-02-mockito.md
docs/lesson-03-spring-boot-test.md
```

Before continuing a learning session, read at least:

```text
README.md
docs/session-progress.md
docs/learning-method.md
```

## Current Learning Style

Follow the process in:

```text
docs/learning-method.md
```

Default flow for each topic:

```text
1. Explain the concept and when to use it.
2. Connect it to the current project.
3. Give the user a concrete test goal.
4. Let the user implement the test when reasonable.
5. Review the code.
6. Run mvn test.
7. Explain failures by expected vs actual behavior.
8. Update learning docs when the concept is completed.
9. End with a short recap or next task.
```

Prefer guiding the user to write code instead of immediately providing full final implementations. Provide full code when the user is blocked, when the setup is mechanical, or when precision matters.

## Code Quality Rules

## Package Structure

The project uses a lightweight DDD / Clean Architecture package structure. Keep new code in the appropriate layer:

```text
com.example.testinglab
├── common.error
├── order.domain
├── order.application
├── order.interfaces.rest
├── product.domain
├── notification.domain
└── notification.application
```

Layer rules:

- `domain`: entities, domain services/rules, domain exceptions, repository interfaces.
- `application`: application services and use-case orchestration.
- `interfaces.rest`: Spring MVC controllers, request DTOs, response DTOs.
- `common.error`: shared error response and global exception handling.

Tests should mirror the production package when practical:

```text
order.domain -> OrderCalculatorTest
order.application -> OrderServiceTest
order.interfaces.rest -> OrderControllerTest
notification.application -> OrderNotificationServiceTest
```

Do not add new production classes directly under `order`, `product`, `notification`, or `common` root packages unless there is a clear reason.

Keep test code clean.

Do not leave old commented-out implementations in test classes. Put comparisons and explanations in docs instead.

When introducing a new learning topic, prefer adding new code instead of modifying already completed examples.

Use one of these approaches:

- add a new test method
- add a dedicated demo test class
- add a small focused production class for the concept

Avoid repeatedly rewriting tests that already passed and represent earlier learning milestones. Modify existing code only when it is wrong, blocks future work, needs small cleanup, or the user explicitly asks for refactoring.

Use existing project style. Do not introduce unnecessary abstractions.

For Java tests:

- Use JUnit 5.
- Use AssertJ for assertions.
- Use Mockito for mocking.
- Keep tests focused on one behavior.
- Prefer clear test names such as `shouldCreateOrderWhenProductExistsAndStockIsEnough`.

## Verification

After code changes, run:

```bash
mvn test
```

Report the important result to the user, especially:

```text
Tests run, Failures, Errors, Skipped
```

If tests fail, explain:

```text
1. What failed?
2. What was expected?
3. What actually happened?
4. Whether the issue is in business code, test code, test data, or environment.
5. What to change next.
```

## Documentation Rules

Keep README general. Do not put device-specific paths, current progress details, or latest test results in README.

Use `docs/session-progress.md` for:

- device-specific environment details
- current test result
- exact learning progress
- next task
- handoff information for another AI/Agent

Use lesson docs for explanations and examples:

```text
docs/lesson-01-junit5.md
docs/lesson-02-mockito.md
```

Use `docs/learning-plan.md` for the roadmap.

Use `docs/learning-method.md` for learning process improvements.

When a meaningful learning milestone is completed, update the relevant docs.

## Current Topic

The project is currently in Spring Boot testing.

The next planned topic is request body JSON testing for `POST /api/orders`, based on the current `docs/session-progress.md`.

Before proceeding, check the actual current code and test status because the user may have made changes after the progress document was last updated.

## Important Project Notes

Mockito is configured with:

```text
src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
```

Content:

```text
mock-maker-subclass
```

This avoids inline mock maker Java agent attachment issues in the current environment.

Do not remove this file unless there is a clear reason and tests confirm Mockito still works.
