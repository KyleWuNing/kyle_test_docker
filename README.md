# Kyle Test Docker

Local development stack with MySQL, Redis, Kafka, PostgreSQL, and LiteLLM proxy.

## Services

| Service    | Image                   | Port |
|------------|-------------------------|------|
| MySQL      | mysql:8.0               | 3306 |
| Redis      | redis:7.0-alpine        | 6379 |
| Kafka      | apache/kafka:latest     | 9092 |
| PostgreSQL | postgres:15-alpine      | 5432 |
| LiteLLM    | litellm/litellm:latest  | 4000 |

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

## Quick Start

```bash
docker compose up -d
```

## LiteLLM Proxy

LiteLLM runs at `http://localhost:4000` and proxies requests to 10 models across 4 providers.

**UI:** http://localhost:4000/ui  
**Login:** `admin` / `admin`  
**Master key:** `sk-local-master-key`

### Configured Models

| Provider  | Models                                                      |
|-----------|-------------------------------------------------------------|
| OpenAI    | `gpt-4o`, `gpt-4o-mini`, `o3-mini`                         |
| Anthropic | `claude-opus-4-8`, `claude-sonnet-4-6`, `claude-haiku-4-5` |
| Google    | `gemini-2.5-pro`, `gemini-2.0-flash`                       |
| DeepSeek  | `deepseek-v3`, `deepseek-r1`                               |

### Setting Real API Keys

Replace the dummy keys in `docker-compose.yml` with your real keys, then restart LiteLLM:

```bash
docker compose up -d litellm
```

### Example API Call

```bash
curl http://localhost:4000/v1/chat/completions \
  -H "Authorization: Bearer sk-local-master-key" \
  -H "Content-Type: application/json" \
  -d '{"model": "gpt-4o", "messages": [{"role": "user", "content": "Hello!"}]}'
```

## Database Credentials

| DB         | Host      | Port | User     | Password | Database |
|------------|-----------|------|----------|----------|----------|
| MySQL      | localhost | 3306 | root     | root     | local_db |
| PostgreSQL | localhost | 5432 | postgres | root     | local_db |

## Useful Commands

```bash
# Start all services
docker compose up -d

# Stop all services
docker compose down

# View logs
docker compose logs -f litellm

# List running containers
docker compose ps
```