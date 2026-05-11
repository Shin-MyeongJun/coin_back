# Observability Stack

This stack adds infrastructure monitoring for the local Docker environment and can scrape the API module's Spring Actuator Prometheus endpoint when the API is running on the host.

## Scope

Included:

- Prometheus
- Grafana
- Kafka consumer lag through `kafka-exporter`
- Redis memory, key count, command throughput, and latency signals through `redis-exporter`
- API JVM/HTTP/application metrics through `/actuator/prometheus`

Not included yet:

- heartbeat domain gauges from `infra_heartbeat`
- Grafana panels for application-level metrics

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
- API actuator: http://localhost:8080/actuator/prometheus

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

The Prometheus config also includes a `coindata-api` scrape target for `host.docker.internal:8080`.
That target is expected to be down until the API module is running locally.

## Later module-level additions

Add these in a separate phase:

- `infra_heartbeat` gauges for module health summary
- Grafana panels for heartbeat all-dead/recovered state
