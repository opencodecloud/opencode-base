# OpenCode Base Event

Event-driven architecture support with publish/subscribe pattern and virtual threads for JDK 25+.

## Features

- Publish/Subscribe event bus
- Synchronous and asynchronous event processing
- Virtual thread-based async execution
- Event priority ordering
- Event cancellation support
- Annotation-based listener registration (`@Subscribe`, `@Async`, `@Priority`)
- Lambda listener registration
- Data event wrapper for arbitrary payloads
- Waitable events with timeout support
- Event sourcing with pluggable event store
- In-memory event store implementation
- Saga pattern support (multi-step transactions with compensation)
- Event serialization support
- Event security: rate limiting, signed events, verifiable events
- Configurable exception handlers (logging, retry)
- Heartbeat monitoring
- Builder API for custom event bus configuration
- Singleton and per-instance modes
- AutoCloseable lifecycle management
- Thread-safe

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-event</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

| Class | Description |
|-------|-------------|
| `OpenEvent` | Event bus facade -- main entry point for publish/subscribe operations |
| `Event` | Base event class with ID, timestamp, source, and cancellation |
| `DataEvent` | Generic data-carrying event wrapper |
| `WaitableEvent` | Event that supports blocking wait for processing completion |
| `EventListener` | Functional event listener interface |
| **Annotations** | |
| `@Subscribe` | Marks a method as an event listener |
| `@Async` | Marks a listener for asynchronous execution |
| `@Priority` | Sets listener execution priority |
| **Dispatchers** | |
| `EventDispatcher` | Event dispatcher interface |
| `SyncDispatcher` | Synchronous event dispatcher |
| `AsyncDispatcher` | Asynchronous event dispatcher using virtual threads |
| **Exception Handlers** | |
| `EventExceptionHandler` | Exception handler interface |
| `LoggingExceptionHandler` | Logs exceptions during event processing |
| `RetryExceptionHandler` | Retries failed event processing |
| **Saga** | |
| `Saga` | Saga orchestrator for multi-step transactions |
| `SagaStep` | Individual step in a saga with action and compensation |
| `SagaResult` | Result of saga execution |
| `SagaStatus` | Saga execution status enum |
| **Security** | |
| `SecureEventBus` | Event bus with security features |
| `EventRateLimiter` | Rate limiter for event publishing |
| `SignedEvent` | Event with cryptographic signature |
| `VerifiableEvent` | Event that can be verified for authenticity |
| **Serialization** | |
| `EventSerializer` | Event serialization interface |
| **Store** | |
| `EventStore` | Event store interface for event sourcing |
| `InMemoryEventStore` | In-memory event store implementation |
| `EventRecord` | Stored event record |
| **Monitor** | |
| `HeartbeatMonitor` | Heartbeat monitor for event bus health |
| **Exceptions** | |
| `EventException` | General event exception |
| `EventListenerException` | Listener-specific exception |
| `EventPublishException` | Publish-specific exception |
| `EventSecurityException` | Security-related exception |
| `EventStoreException` | Event store exception |
| `EventErrorCode` | Error code enum |

## Quick Start

```java
import cloud.opencode.base.event.OpenEvent;
import cloud.opencode.base.event.Event;

// Get default event bus
OpenEvent eventBus = OpenEvent.getDefault();

// Register lambda listener
eventBus.on(UserRegisteredEvent.class, event -> {
    System.out.println("User registered: " + event.getUserId());
});

// Register annotation-based listener
eventBus.register(new MyEventHandler());

// Publish event synchronously
eventBus.publish(new UserRegisteredEvent(userId, email));

// Publish event asynchronously
eventBus.publishAsync(event)
    .thenRun(() -> System.out.println("Event processed"));

// Publish and wait with timeout
boolean completed = eventBus.publishAndWait(event, Duration.ofSeconds(5));

// Publish arbitrary data
eventBus.publish("Hello, World!");

// Custom event bus with event store
OpenEvent custom = OpenEvent.builder()
    .eventStore(new InMemoryEventStore(10000))
    .exceptionHandler(new LoggingExceptionHandler())
    .build();

// Annotation-based handler class
public class MyEventHandler {
    @Subscribe
    @Async
    @Priority(10)
    public void onUserRegistered(UserRegisteredEvent event) {
        // Process asynchronously with high priority
    }
}
```

## Requirements

- Java 25+

## License

Apache License 2.0
