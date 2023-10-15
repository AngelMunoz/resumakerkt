package com.github.angelmunoz.resumakerkt.types

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.serialization.Serializable

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


data class GenerateParams(val outDir: String, val template: String, val language: List<String>)

fun interface ResumeLocator {
    fun getResume(path: String): List<Resume>
}

fun interface AsyncResumeLocator {
    suspend fun getResume(path: String): List<Resume>
}

fun interface TemplateRenderer {
    fun render(templateNameOrPath: String, payload: Resume): String
}

fun interface PdfConverter {
    fun convert(html: String, outPath: String): String
}

interface ApplicationEnvironment {
    val resumeLocator: ResumeLocator
    val templateRenderer: TemplateRenderer
    val asyncResumeLocator: AsyncResumeLocator
    val pdfConverter: PdfConverter
    fun <TLogOwner> getLogger(cls: Class<TLogOwner>): KLogger
}
