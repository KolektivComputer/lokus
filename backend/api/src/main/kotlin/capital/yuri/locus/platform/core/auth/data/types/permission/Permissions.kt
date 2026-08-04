package capital.yuri.locus.platform.core.auth.data.types.permission

enum class Permissions(val category: PermissionCategory, val displayName: String) {
    UPLOAD_THEMES(PermissionCategory.WebContent, "Upload themes"),
}
