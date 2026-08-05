# locus example stack

Local Postgres + API for development.

```bash
# recommended — embeds git commit/tag into the image when possible
chmod +x up.sh
./up.sh --build

# equivalent manual form
LOCUS_COMMIT=$(git rev-parse HEAD) docker compose up --build

# DB only (run the JVM against compose Postgres)
docker compose up postgres
```

CLI shape (matches Clikt):
```text
locus api [<options>] <configdirectory>
```
Compose passes: `api --host 0.0.0.0 --port 8080 /app/config`

| Path | Purpose |
|------|--------|
| `config/` | Mounted at `/app/config` in the API container |
| `extensions/` | Mounted at `/app/extensions` for extension JARs |

API: http://127.0.0.1:8080  ·  Postgres: `localhost:5432` / `locus` / `locus` / `locus`
