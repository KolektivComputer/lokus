package capital.yuri.locus.platform.core.config

enum class ConfigType {
    /** Stored at CONFIG_ROOT/<name>.jsonc */
    Root,

    /** Stored at CONFIG_ROOT/ext/<extensionId>/<name>.jsonc */
    Extension,
}
