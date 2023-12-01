package me.aburke.hotelbooking.facade.rest.snapshot

import java.io.File

private val snapshotsRoot = System.getenv("TEST_SNAPSHOT_DIR")

class SnapshotFile(fnamePrefix: String, private val specType: String) {

    private val specFname = "$fnamePrefix.$specType.spec"
    private val file = File("$snapshotsRoot/$specFname")
    private val newSpecFile = File("$snapshotsRoot/$fnamePrefix.$specType.spec.new")
    var newSnapshot: String? = null
    private val ignoredLines: MutableSet<Int> = mutableSetOf()

    fun ignoreLine(lineNo: Int) = ignoredLines.add(lineNo)

    fun verify() {
        newSnapshot?.let { newSnapshot ->
            file.parentFile.mkdirs()
            if (!file.exists()) {
                println("No snapshot exists for $specFname; writing result to new spec file")
                newSpecFile.writeText(newSnapshot)
                return
            }

            val oldSnapshot = file.readText()
            if (!valuesEquivalent(oldSnapshot, newSnapshot)) {
                println("Writing mismatch spec to $specFname.new")
                newSpecFile.writeText(newSnapshot)
                throw AssertionError(
                    "${specType.replaceFirstChar { it.titlecase() }} spec does not match the new data",
                )
            }

            if (newSpecFile.exists()) {
                println("Deleting new spec file for $specFname")
                newSpecFile.delete()
            }
        }
    }

    private fun valuesEquivalent(rawOldValue: String, rawNewValue: String): Boolean =
        if (ignoredLines.isEmpty()) {
            rawOldValue == rawNewValue
        } else {
            normalizedValue(rawOldValue) == normalizedValue(rawNewValue)
        }

    private fun normalizedValue(rawValue: String) = rawValue.split('\n')
        .filterIndexed { i, _ -> !ignoredLines.contains(i) }
}
