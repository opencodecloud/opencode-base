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
- **Subscription handles** (AutoCloseable) for precise lifecycle management
- **Dead event detection** -- unhandled events are wrapped as `DeadEvent` and re-dispatched
- **Event filtering** -- subscribe with `Predicate` to selectively receive events
- **Interceptor chain** -- pre/post publish hooks for cross-cutting concerns
- **Sticky events** -- late subscribers receive the last event of a given type
- **Event bus metrics** -- operational statistics (published, delivered, errors, dead events)
- **Test utilities** -- `EventCaptor` for capturing and asserting events in tests
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
- Exception hierarchy extends `OpenException` (unified OpenCode exception base)

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-event</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API Overview

| Class | Description |
|-------|-------------|
| `OpenEvent` | Event bus facade -- main entry point for publish/subscribe operations |
| `Event` | Base event class with ID, timestamp, source, and cancellation |
| `DataEvent` | Generic data-carrying event wrapper |
| `WaitableEvent` | Event that supports blocking wait for processing completion |
| `DeadEvent` | Wraps events that have no subscribers (V1.0.3) |
| `EventListener` | Functional event listener interface |
| `Subscription` | AutoCloseable subscription handle (V1.0.3) |
| **Annotations** | |
| `@Subscribe` | Marks a method as an event listener |
| `@Async` | Marks a listener for asynchronous execution |
| `@Priority` | Sets listener execution priority |
| **Dispatchers** | |
| `EventDispatcher` | Event dispatcher interface |
| `SyncDispatcher` | Synchronous event dispatcher |
| `AsyncDispatcher` | Asynchronous event dispatcher using virtual threads |
| **Interceptors** | |
| `EventInterceptor` | Pre/post publish interceptor interface (V1.0.3) |
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
| `EventBusMetrics` | Event bus statistics snapshot (V1.0.3) |
| **Testing** | |
| `EventCaptor` | Test utility for capturing and asserting events (V1.0.3) |
| **Exceptions** | |
| `EventException` | General event exception (extends `OpenException`) |
| `EventListenerException` | Listener-specific exception |
| `EventPublishException` | Publish-specific exception |
| `EventSecurityException` | Security-related exception |
| `EventStoreException` | Event store exception |
| `EventErrorCode` | Error code enum |

## Quick Start

```java
import cloud.opencode.base.event.*;

// Get default event bus
OpenEvent eventBus = OpenEvent.getDefault();

// Register lambda listener
eventBus.on(UserRegisteredEvent.class, event -> {
    System.out.println("User registered: " + event.getUserId());
});

// Subscribe with Subscription handle (precise unsubscription)
Subscription sub = eventBus.subscribe(OrderEvent.class, event -> {
    processOrder(event);
});
sub.unsubscribe(); // or use try-with-resources

// Subscribe with filter
eventBus.subscribe(OrderEvent.class,
    event -> processLargeOrder(event),
    event -> event.getAmount() > 1000);

// Dead event detection
eventBus.subscribe(DeadEvent.class, dead -> {
    log.warn("Unhandled event: {}", dead.getOriginalEvent());
});

// Sticky events
eventBus.publishSticky(new ConfigEvent(config));
// Late subscriber receives immediately:
eventBus.subscribe(ConfigEvent.class, e -> applyConfig(e));

// Publish event
eventBus.publish(new UserRegisteredEvent(userId, email));

// Async publish
eventBus.publishAsync(event)
    .thenRun(() -> System.out.println("Event processed"));

// Metrics
EventBusMetrics metrics = eventBus.getMetrics();
System.out.println("Published: " + metrics.totalPublished());

// Custom event bus with interceptor
OpenEvent custom = OpenEvent.builder()
    .eventStore(new InMemoryEventStore(10000))
    .exceptionHandler(new LoggingExceptionHandler())
    .interceptor(event -> { log.info("Publishing: {}", event); return true; })
    .build();

// Test utility
EventCaptor<MyEvent> captor = new EventCaptor<>();
eventBus.subscribe(MyEvent.class, captor);
eventBus.publish(new MyEvent());
assertThat(captor.count()).isEqualTo(1);
```

## OpenEvent Method Reference

### Factory Methods

| Method | Description |
|--------|-------------|
| `OpenEvent.getDefault()` | Get the shared singleton event bus instance |
| `OpenEvent.create()` | Create a new independent event bus instance |
| `OpenEvent.builder()` | Create a builder for customized event bus configuration |

### Listener Registration

| Method | Description |
|--------|-------------|
| `subscribe(Class<E>, EventListener<E>)` | Subscribe and return a `Subscription` handle |
| `subscribe(Class<E>, EventListener<E>, Predicate<E>)` | Subscribe with event filter predicate |
| `subscribe(Class<E>, EventListener<E>, Predicate<E>, boolean, int)` | Subscribe with filter, async flag, and priority |
| `on(Class<E>, EventListener<E>)` | Register lambda listener (legacy, no return) |
| `on(Class<E>, EventListener<E>, boolean)` | Register lambda listener with async option |
| `on(Class<E>, EventListener<E>, boolean, int)` | Register lambda listener with async and priority |
| `register(Object)` | Register annotation-based subscriber (`@Subscribe`) |
| `unregister(Object)` | Unregister all listeners from a subscriber |

### Event Publishing

| Method | Description |
|--------|-------------|
| `publish(Event)` | Publish event synchronously |
| `publishAsync(Event)` | Publish event asynchronously, returns `CompletableFuture` |
| `publish(T)` | Publish arbitrary data wrapped as `DataEvent` |
| `publish(T, String)` | Publish data with source identifier |
| `publishAndWait(Event, Duration)` | Publish and block until processing completes or timeout |
| `publishSticky(Event)` | Publish sticky event (stored + replayed to future subscribers) |

### Sticky Events

| Method | Description |
|--------|-------------|
| `publishSticky(Event)` | Publish and store a sticky event (last-one-wins per type) |
| `getStickyEvent(Class<E>)` | Get the last sticky event of a given type, or null |
| `removeStickyEvent(Class<E>)` | Remove and return the sticky event of a given type |

### Interceptors

| Method | Description |
|--------|-------------|
| `addInterceptor(EventInterceptor)` | Add a pre/post publish interceptor |
| `removeInterceptor(EventInterceptor)` | Remove an interceptor |

### Metrics

| Method | Description |
|--------|-------------|
| `getMetrics()` | Get current metrics snapshot (`EventBusMetrics` record) |
| `resetMetrics()` | Reset all metric counters to zero |

### Configuration

| Method | Description |
|--------|-------------|
| `setEventStore(EventStore)` | Set the event store for event sourcing |
| `getEventStore()` | Get the current event store |
| `setExceptionHandler(EventExceptionHandler)` | Set the exception handler |
| `close()` | Shut down dispatchers and executor, release resources |

### Builder Options

| Method | Description |
|--------|-------------|
| `asyncExecutor(ExecutorService)` | Custom async executor |
| `syncDispatcher(EventDispatcher)` | Custom sync dispatcher |
| `asyncDispatcher(EventDispatcher)` | Custom async dispatcher |
| `eventStore(EventStore)` | Event store for event sourcing |
| `exceptionHandler(EventExceptionHandler)` | Custom exception handler |
| `interceptor(EventInterceptor)` | Add interceptor during construction |

## Requirements

- Java 25+

## License

Apache License 2.0
