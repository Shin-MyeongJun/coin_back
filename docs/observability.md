# Observability Stack

This stack adds infrastructure-only monitoring for the local Docker environment.
It does not change application module code, Gradle dependencies, or module `application.yml` files.

## Scope

Included:

- Prometheus
- Grafana
- Kafka consumer lag through `kafka-exporter`
- Redis memory, key count, command throughput, and latency signals through `redis-exporter`

Not included yet:

- Spring Actuator or Micrometer inside application modules
- heartbeat domain gauges from `infra_heartbeat`

Those require application-module changes and should be handled as a later phase.

## Run

From the repository root:

```powershell
cd docker
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

URLs:

- Prometheus: http://localhost:9090
- Prometheus targets: http://localhost:9090/targets
- Grafana: http://localhost:3000
- Kafka exporter: http://localhost:9308/metrics
- Redis exporter: http://localhost:9121/metrics

Grafana login:

- User: `admin`
- Password: `admin`

## Kafka listener note

The base Docker Compose file exposes Kafka to host applications through `localhost:9092`.
The observability overlay keeps that host listener and adds an internal Docker listener at `kafka:29092`.
The Kafka exporter uses the internal listener so it can collect consumer lag from inside the Compose network.

## Dashboard

Grafana is provisioned automatically with:

- Data source: `Prometheus`
- Dashboard: `CoinData Infra Overview`

The dashboard focuses on metrics that do not require app-module changes:

- exporter health
- Kafka consumer lag by group
- Kafka consumer lag by topic
- Redis memory usage
- Redis key count
- Redis command throughput
- Redis latency signals
- Redis connected clients

## Later module-level additions

When module changes are acceptable, add these in a separate phase:

- Spring Actuator + Micrometer dependencies/config per runtime module
- Prometheus scrape targets for `/actuator/prometheus`
- `infra_heartbeat` gauges for module health summary
- Grafana panels for heartbeat all-dead/recovered state
