package com.github.angelmunoz.resumakerkt.types

import arrow.core.Either
import kotlinx.serialization.Serializable
import org.w3c.dom.Document
import java.io.File
import java.io.OutputStream

@Serializable
data class Project(
        val name: String,
        val description: String,
        val stack: String,
        val url: String,
)

@Serializable
data class Link(val name: String, val url: String)

@Serializable
data class Job(
        val employer: String,
        val position: String,
        val description: String,
        val startDate: String? = null,
        val endDate: String? = null,
)

@Serializable
data class Skill(val name: String, val experience: String)

@Serializable
data class Profile(
        val name: String,
        val lastName: String,
        val pitch: String,
        val email: String
)

@Serializable
data class Language(val name: String, val keywords: List<String>)

@Serializable
data class Resume(
        val language: Language,
        val profile: Profile,
        val skills: List<Skill>,
        val jobs: List<Job>,
        val projects: List<Project>?,
        val devLinks: List<Link>?,
        val socialMedia: List<Link>?
)

enum class LogLevel {
    Debug,
    Trace,
    Info
}

data class GenerateParams(val outDir: String, val template: String, val language: List<String>)

@JvmInline
value class ResumeError(val error: String)

@JvmInline
value class PdfConvertError(val error: String)

@JvmInline
value class TemplateRenderingError(val error: String)

@JvmInline
value class FileLocatorError(val error: String)

fun interface ResumeLocator {
    fun getResume(path: String): Either<ResumeError, List<Resume>>
}

fun interface TemplateRenderer {
    fun render(templateNameOrPath: String, payload: Resume): Either<TemplateRenderingError, String>
}

fun interface PdfConverter {
    fun convert(html: String, outPath: String): Either<PdfConvertError, String>
}

fun interface DocumentParser {
    fun parse(html: String): Either<String, Document>
}

fun interface FileLocator {
    fun find(path: String, ensureParent: Boolean): Either<FileLocatorError, File>
}
