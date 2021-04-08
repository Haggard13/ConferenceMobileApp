package com.example.conference.file

data class Addition(
    val file: ByteArray,
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Addition

        if (!file.contentEquals(other.file)) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}