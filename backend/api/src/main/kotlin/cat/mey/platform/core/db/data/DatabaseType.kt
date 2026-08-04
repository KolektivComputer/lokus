package cat.mey.platform.core.db.data

enum class DatabaseType(
    val driver: String,
) {
    PostgreSQL("org.postgresql.Driver"),
}