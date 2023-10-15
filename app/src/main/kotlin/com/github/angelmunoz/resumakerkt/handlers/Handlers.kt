/**
 * This package contain our command handlers
 * functions here should have defined inputs -> outputs
 * that will ease testing and maintenance
 *
 * When adding new handlers, think about how will they compose together with other handlers
 * as if they were part of a pipeline, this will help us keep our handlers small and focused.
 */
package com.github.angelmunoz.resumakerkt.handlers

import com.github.angelmunoz.resumakerkt.types.*
import io.github.oshai.kotlinlogging.KLogger


fun generateResume(
    logger: KLogger,
    resumeLocator: ResumeLocator,
    templateRenderer: TemplateRenderer,
    pdfConverter: PdfConverter,
    resume: String,
    params: GenerateParams
): Sequence<String> {

    fun getResumeList(path: String, language: List<String>, resumeLocator: ResumeLocator): List<Resume> {
        val resumeList = resumeLocator.getResume(path)
        return if (language.isEmpty()) {
            logger.info { "No language provided, generating all available languages." }
            resumeList
        } else {
            resumeList.filter { resume ->
                language.contains(resume.language.name)
            }
        }
    }

    return getResumeList(resume, params.language, resumeLocator).asSequence().map {
        logger.info { "Generating resumes for languages: ${it.language.name}" }
        val htmlContent = templateRenderer.render(params.template, it)
        pdfConverter.convert(
            htmlContent, "${params.outDir}/${it.language.name}.pdf"
        )
    }
}

typealias ResumeGenerator = (KLogger, ResumeLocator, TemplateRenderer, PdfConverter, String, GenerateParams) -> Sequence<String>
