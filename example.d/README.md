# locus example stack

Local Postgres + API for development.

```bash
# from this directory
docker compose up --build

# DB only (stop api if you run the JVM against compose Postgres)
docker compose up postgres
```

| Path | Purpose |
|------|--------|
| `config/` | Mounted at `/config` in the API container (writable so first-boot defaults can be written) |
| `extensions/` | Mounted at `/extensions` for extension JARs |

API: http://127.0.0.1:8080  ·  Postgres: `localhost:5432` / `locus` / `locus` / `locus`
