super — evo ti **dva kompletna README.md fajla**, spremna za copy-paste u tvoje repoe:

---

# `config-server/README.md`

## TeamNest Config Service (Spring Cloud Config Server, HA, RabbitMQ Bus, Vault, OTel, Prometheus, Grafana, Loki)

### Sadržaj

* [Pregled](#pregled)
* [Arhitektura](#arhitektura)
* [Preuslovi](#preuslovi)
* [Brzi start (Docker Compose)](#brzi-start-docker-compose)
* [Konfiguracioni Git repo](#konfiguracioni-git-repo)
* [Sigurnost](#sigurnost)
* [Spring Cloud Bus (RabbitMQ)](#spring-cloud-bus-rabbitmq)
* [Vault tajne](#vault-tajne)
* [Observability (OTel → Collector → Prometheus/Grafana; logovi u Loki)](#observability-otel--collector--prometheusgrafana-logovi-u-loki)
* [Actuator endpoints](#actuator-endpoints)
* [CI/CD (Checkstyle + SonarCloud)](#cicd-checkstyle--sonarcloud)
* [Build & Run lokalno (bez Docker-a)](#build--run-lokalno-bez-docker-a)
* [Promena konfiguracije i hot reload](#promena-konfiguracije-i-hot-reload)
* [Troubleshooting](#troubleshooting)

---

## Pregled

Spring Cloud Config Server za sve servise (**gateway + microservices**). Enterprise setup:

* **HA**: dve instance iza **Nginx** reverse proxy-ja (TLS terminacija).
* **Sigurnost**: HTTPS, Basic Auth (po default-u), opcioni **mTLS**; izolovan Docker network.
* **Konfiguracije**: iz **Git** repozitorijuma (branch, credentials).
* **Bus refresh**: **RabbitMQ** za distribuirani refresh konfiguracije.
* **Tajne**: **Vault** (KV v2).
* **Observability**: **OTel agent** u JVM, **OTel Collector**, **Prometheus/Grafana** (metrike), **Loki/Promtail** (logovi).

## Arhitektura

```
klijentski servisi ──HTTPS──> Nginx ──> config-server-1/2 (Spring Cloud Config)
                                  │
                                 TLS
RabbitMQ (Bus)  <──────────────────┘   (event /monitor → refresh)
Vault (tajne)   ←──────── config-server  (Spring Cloud Vault client)
OTel Agent → OTel Collector → Prometheus (metrics) / Loki (logs) / (Tempo opc.)
```

## Preuslovi

* Docker + Docker Compose
* Java 21 (ako hoćeš lokalni run)
* SonarCloud nalog + `SONAR_TOKEN` (ako koristiš CI job)
* Git repo sa konfiguracijama (vidi dole)

## Brzi start (Docker Compose)

1. Postavi `.env` pored `infra/docker/docker-compose.yml`:

```env
CONFIG_GIT_URI=https://github.com/<tvoj-user>/<config-repo>.git
CONFIG_GIT_USERNAME=<git-user>
CONFIG_GIT_PASSWORD=<git-token-or-pass>
CONFIG_USER=config
CONFIG_PASSWORD=superSecret
RABBIT_USER=teamnest
RABBIT_PASS=teamnest
VAULT_TOKEN=root
TLS_KEYSTORE_PASSWORD=changeit
TLS_TRUSTSTORE_PASSWORD=changeit
```

2. Generiši TLS materijal:

* Za Nginx: `infra/docker/nginx/certs/server.crt`, `server.key`, `ca.crt`
* Za Config Server (keystore/truststore): `server-keystore.p12`, `server-truststore.p12` u `infra/docker/nginx/certs/`

> Za demo možeš i self-signed. Ako ne koristiš mTLS klijenta, ostavi `ssl_verify_client optional` u `nginx.conf`.

3. Podigni sve:

```bash
cd infra/docker
docker compose up -d --build
```

4. Provera:

* Config Server Health: `https://localhost:8888/actuator/health`
* Prometheus: `http://localhost:9090`
* Grafana: `http://localhost:3000` (user/pass iz `.env` ili default `admin/admin`)
* RabbitMQ UI: `http://localhost:15672`
* Vault UI: `http://localhost:8200`

## Konfiguracioni Git repo

Minimalna struktura:

```
<config-repo>/
  application.yml          # global defaults
  gateway.yml              # specifično za serviceId = 'gateway'
  userservice.yml
  ...
```

Podržani fajlovi: `yml`, `yaml`, `properties`. Profile varijante: `gateway-dev.yml`, `gateway-prod.yml` itd.
Config Server endpoints:

* `/{application}/{profile}`  → npr. `/gateway/dev`
* `/{application}-{profile}.yml` → npr. `/gateway-dev.yml`

## Sigurnost

* **Basic Auth** (default): klijenti šalju `spring.cloud.config.username/password`
* **TLS**: Nginx terminira TLS (443 → backend 8888).
* **mTLS (opciono)**:

  * U `nginx.conf`: `ssl_client_certificate` + `ssl_verify_client on`
  * Klijenti moraju imati client cert potpisan vašim CA i slati ga ka Nginx-u (vidi README klijenta/gateway-a).
* **Actuator**: izložen samo minimalno (`health`, `info`, `prometheus`, `refresh/busrefresh`).

## Spring Cloud Bus (RabbitMQ)

* RabbitMQ radi na `rabbitmq:5672`, UI na `15672`.
* Kada stigne webhook na `/monitor` (iz config Git-a) Bus šalje event → svi klijenti povuku nov config.

## Vault tajne

* Vault u dev modu (za showcase).
* Primer upisa:

```bash
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root
vault kv put secret/gateway db.password='s3cr3t' api.key='abc123'
```

* Klijenti učitavaju preko `spring.cloud.vault` (vidi njihov `bootstrap.yml`).

## Observability (OTel → Collector → Prometheus/Grafana; logovi u Loki)

* **Tracing**: OTel Java agent u containeru (env `JAVA_TOOL_OPTIONS`), export u **OTel Collector** (OTLP).
* **Metrics**: Micrometer Prometheus → `/actuator/prometheus`. Prometheus scrape-uje Nginx → backend.
* **Logs**: Docker logs čita Promtail → šalje u Loki. Gledaj u Grafani (datasource: Loki).

## Actuator endpoints

* `GET /actuator/health` (liveness/readiness)
* `GET /actuator/info`
* `GET /actuator/prometheus`
* `POST /actuator/refresh` (lokalno)
* `POST /actuator/busrefresh` (distribuirano)
* `POST /monitor` (Git webhook)

## CI/CD (Checkstyle + SonarCloud)

* Maven plugin `maven-checkstyle-plugin` (fail on error).
* SonarCloud step u `.github/workflows/ci.yml` (potreban `SONAR_TOKEN` u repo secrets).
  Pokretanje lokalno:

```bash
./mvnw -B -ntp clean verify
./mvnw -B -ntp org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=<tvoja_org> \
  -Dsonar.projectKey=<tvoj_key>
```

## Build & Run lokalno (bez Docker-a)

```bash
# u rootu modula config-server/
./mvnw -B -ntp clean package
java -jar target/config-server-1.0.0.jar
```

Potrebni ENV varovi: `CONFIG_GIT_URI`, `CONFIG_USER`, `CONFIG_PASSWORD`, Rabbit/Vault ako su uključeni, itd.

## Promena konfiguracije i hot reload

1. Commit/push u **config repo**
2. Git webhook tada gađa `POST /monitor` (preko ngrok-a ako testiraš eksterno)
3. Spring Cloud Bus preko RabbitMQ šalje refresh event → svi servisi učitaju novi config.

## Troubleshooting

* **“Cannot clone git”**: proveri `CONFIG_GIT_*` varijable i pristup tokenu.
* **“401 from Config Server”**: klijent nema ispravan `spring.cloud.config.username/password`.
* **mTLS problemi**: proveri CA chain, `ssl_verify_client` i client cert.
* **Prometheus ne vidi metrike**: `prometheus.yml` target `nginx` + `scheme: https` + `insecure_skip_verify: true` za self-signed.
* **Bus ne radi**: proveri RabbitMQ kredencijale i da je `spring-cloud-bus-amqp` dodan klijentima.

---

# `gateway/README.md`

## TeamNest Gateway (Spring Cloud Gateway, Resilience, Rate Limiting, Security, OTel, Prometheus, Loki)

### Sadržaj

* [Pregled](#pregled)
* [Funkcionalnosti](#funkcionalnosti)
* [Preuslovi](#preuslovi)
* [Konfiguracija (preko Config Server-a + Vault)](#konfiguracija-preko-config-server-a--vault)
* [Build & Test](#build--test)
* [Pokretanje lokalno](#pokretanje-lokalno)
* [Docker Run](#docker-run)
* [Observability](#observability)
* [Sigurnost](#sigurnost-1)
* [CI/CD (Checkstyle + SonarCloud)](#cicd-checkstyle--sonarcloud)
* [Troubleshooting](#troubleshooting-1)

---

## Pregled

Enterprise gateway za demo: reverse proxy/routing, resiliency pattern-i, rate limiting, centralizovana konfiguracija, observability i sigurnost.

## Funkcionalnosti

* **Spring Cloud Gateway** (WebFlux)
* **Resilience4j / Spring Cloud CircuitBreaker**: circuit-breaker, retry
* **Rate Limiter** (Redis ili in-memory; u IT testovima koristimo Testcontainers/Redis)
* **Global error handling** sa RFC-7807 problem detaljima
* **Security** (Spring Security): minimalna default zaštita end-pointova; CORS, headers
* **Centralizovana konfiguracija** preko **Config Server-a** (+ hot reload preko Bus-a)
* **Observability**: Micrometer Prometheus, OTel tracing, logovi u Loki
* Testovi: **unit + integration** (JUnit5, Testcontainers za Redis)

## Preuslovi

* Podignut **Config Server** (vidi njegov README)
* RabbitMQ (za Bus), Vault (ako koristiš tajne), Prometheus/Grafana, Loki/Promtail po želji

## Konfiguracija (preko Config Server-a + Vault)

U `bootstrap.yml` (u samom gateway projektu) već je postavljeno:

```yaml
spring:
  application:
    name: gateway
  cloud:
    config:
      uri: https://localhost:8888
      username: ${CONFIG_USER}
      password: ${CONFIG_PASSWORD}
      fail-fast: true
      retry:
        max-attempts: 12
        initial-interval: 500ms
        multiplier: 1.5
management:
  endpoints:
    web.exposure.include: "health,info,refresh,busrefresh,prometheus"
```

### Primeri fajlova u config repou

`gateway.yml` (shared za sve profile):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: users
          uri: http://userservice:8080
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - CircuitBreaker=name=usersCB, fallbackUri=forward:/fallback/users
            - Retry=name=usersRetry,retries=3,methods=GET
      default-filters:
        - RemoveRequestHeader=Cookie
        - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials
  redis:
    host: redis
    port: 6379

rateLimiter:
  replenishRate: 10
  burstCapacity: 20
```

`gateway-dev.yml`:

```yaml
logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: INFO
cors:
  allowed-origins: "*"
  allowed-methods: "GET,POST,PUT,DELETE"
```

**Tajne** (Vault, KV v2), npr. `secret/gateway`:

```bash
vault kv put secret/gateway jwt.secret='super-strong-secret' redis.password='pass'
```

… i u aplikaciji mapirati kroz `spring.cloud.vault` (već enable-ovan u parent setup-u) ili reference u config-u.

## Build & Test

```bash
./mvnw -B -ntp clean verify
```

* **Checkstyle** se izvršava u `verify` fazi; build failuje na pravila.
* **JaCoCo** generiše coverage report (Sonar ga čita).
* Integration testovi koriste **Testcontainers** (povlače Docker redis image).

## Pokretanje lokalno

Bez Docker-a:

```bash
# Uveri se da Config Server radi i da gateway vidi https://localhost:8888
export CONFIG_USER=config
export CONFIG_PASSWORD=superSecret
./mvnw -B -ntp spring-boot:run
```

App će pokrenuti Netty server (port npr. `8080` ili prema `server.port` iz config-a).

## Docker Run

Primer `Dockerfile` (ako već nije u repou):

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR=target/gateway-0.0.1-SNAPSHOT.jar
COPY ${JAR} app.jar
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.7.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Run:

```bash
docker build -t teamnest/gateway:latest .
docker run --rm -p 8080:8080 \
  -e CONFIG_USER=config \
  -e CONFIG_PASSWORD=superSecret \
  -e OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318 \
  teamnest/gateway:latest
```

## Observability

* **Metrike**: `GET /actuator/prometheus` (Prometheus scrape)
* **Tracing**: OTel agent → Collector (OTLP 4318/4317)
* **Logovi**: standard output → Promtail → Loki (preko Docker driver-a)

Grafana:

* Datasource: Prometheus (`http://prometheus:9090`)
* Datasource: Loki (`http://loki:3100`)
* Dashboard: Micrometer JVM/HTTP (dodaćemo provisioning po potrebi)

## Sigurnost

* **CORS** i **HTTP header** hardening preko Gateway filtera.
* **Globalni exception handler** vraća **RFC-7807** problem+JSON.
* Ako želiš **mTLS** klijent→Config Server strogo: konfiguriši client cert u JVM truststore/keystore i koristi `spring.cloud.config.tls.*` (ili preko `WebClientCustomizer`).

## CI/CD (Checkstyle + SonarCloud)

U monorepo CI (`.github/workflows/ci.yml`) će pokretati:

* `./mvnw clean verify` (checkstyle + tests + jacoco)
* SonarCloud skener (ako `SONAR_TOKEN` postoji i nije PR iz forka)

Lokalno Sonar (opciono):

```bash
./mvnw -B -ntp org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=<tvoja_org> \
  -Dsonar.projectKey=<tvoj_key>
```

## Troubleshooting

* **`Unable to fetch config from server`**: Proveri `CONFIG_USER/PASSWORD`, cert chain (ako je self-signed), da li je Config Server dostupan.
* **`refresh` ne radi**: Radi li RabbitMQ? Da li su klijenti dodali `spring-cloud-starter-bus-amqp` i expose-ovali `/actuator/busrefresh`?
* **Redis rate limiter IT test** pada: proveri Docker daemon (Testcontainers treba Docker soket), ili privremeno disable-uj test.
* **CORS**: dodaj/izmeni allowed origins u config repou pa `POST /actuator/busrefresh`.

---

## Šta je sledeće?

Ako želiš, mogu odmah da ti:

* ubacim **primer config repoa** (sa `application.yml`, `gateway.yml`, `*-dev.yml`, `*-prod.yml`),
* dodam **Grafana provisioning** (datasources + par dashboarda),
* pojačam **mTLS** primer (klijent truststore/keystore setup i `nginx.conf` restrikcije),
* ili da ti sastavim **Makefile** sa kratkim komandama (`make up`, `make down`, `make logs`, `make refresh`).


```
{
"data": {
"CONFIG_GIT_URI": "https://github.com/MarkoBozic90/teamnest-config-repo-demo",
"CONFIG_GIT_BRANCH": "main",
"CONFIG_GIT_USERNAME": "my-git-user",
"CONFIG_GIT_PASSWORD": "my-git-pass",
"RABBIT_HOST": "localhost",
"RABBIT_PORT": "5672",
"RABBIT_USER": "guest",
"RABBIT_PASS": "guest"
}
}
```