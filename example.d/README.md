# locus example stack

Local Postgres + API for development.

```bash
# from this directory
docker compose up --build

# DB only (run the JVM against compose Postgres)
docker compose up postgres
```

CLI shape (matches Clikt):
```text
locus api [<options>] <configdirectory>
```
Compose passes: `api --host 0.0.0.0 --port 8080 /config`

| Path | Purpose |
|------|--------|
| `config/` | Mounted at `/config` in the API container |
| `extensions/` | Mounted at `/extensions` for extension JARs |

API: http://127.0.0.1:8080  ·  Postgres: `localhost:5432` / `locus` / `locus` / `locus`
