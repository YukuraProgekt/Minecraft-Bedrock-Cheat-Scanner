package com.rollixmc.anticheat

data class ScanResult(
    val path: String,
    val reason: String,
    val md5Hash: String,
    val type: ResultType
) {
    // For backward compatibility
    val matchedKeyword: String get() = md5Hash
}

enum class ResultType {
    FILE,
    PACKAGE
}
