# ADR-015 — `pullDomainEvents()` Clearing Semantics in AggregateRoot

**Status:** Accepted

---

## Context

Aggregates accumulate domain events during command processing. The infrastructure layer (Kafka publisher) must retrieve and publish these events after the aggregate is saved. If events are not cleared after retrieval, they risk being published multiple times.

---

## Decision

`AggregateRoot.pullDomainEvents()` follows **destructive-read semantics**: it returns the current event list and immediately clears the internal collection.

```java
public List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> events = Collections.unmodifiableList(new ArrayList<>(domainEvents));
    domainEvents.clear();
    return events;
}
```

The infrastructure publication flow is:

```
1. repository.save(aggregate)      // persists state
2. aggregate.pullDomainEvents()    // retrieves and clears events
3. publisher.publish(events)       // sends to Kafka
```

Steps 1 and 3 are not wrapped in a distributed transaction. This is a **best-effort at-least-once delivery** model: if the process crashes between save and publish, the event is lost. A transactional outbox pattern is deferred to Phase 3.

---

## Consequences

- Calling `pullDomainEvents()` twice returns events on the first call and an empty list on the second — no double-publish risk within a single request
- Tests must call `aggregate.pullDomainEvents()` to assert emitted events, which doubles as a correctness check that events are being cleared
- The at-least-once gap (crash between save and publish) is accepted for Phase 1/2; Phase 3 introduces the transactional outbox to close it
