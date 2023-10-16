/**
 * This package contain our command handlers
 * functions here should have defined inputs -> outputs
 * that will ease testing and maintenance
 *
 * When adding new handlers, think about how will they compose together with other handlers
 * as if they were part of a pipeline, this will help us keep our handlers small and focused.
 */
package com.github.angelmunoz.resumakerkt.handlers

import arrow.core.*
import com.github.angelmunoz.resumakerkt.types.*
import io.github.oshai.kotlinlogging.KLogger


fun generateResume(
        logger: KLogger,
        resumeLocator: ResumeLocator,
        templateRenderer: TemplateRenderer,
        pdfConverter: PdfConverter,
        resumePath: String,
        params: GenerateParams
) {
    val (outDir, template, language)  = params

    val resumeList = resumeLocator
        .getResume(resumePath)
        .mapLeft { it.error }
        .getOrElse { emptyList() }

    val filtered =
            if (language.isEmpty()) {
                logger.info { "No language provided, generating all available languages." }
                resumeList
            } else {
                resumeList.filter { resume ->
                    language.contains(resume.language.name)
                }
            }
    for (resume in filtered) {
        logger.info { "Generating resumes for languages: ${resume.language.name}" }
        val renderResult =
            templateRenderer
                .render(template, resume)
                .mapLeft { it.error }
                .flatMap { htmlContent ->
                    pdfConverter.convert(
                        htmlContent, "${outDir}/${resume.language.name}.pdf"
                    )
                        .mapLeft { it.error }
                }
        when(renderResult) {
            is Either.Right -> logger.info { "Generated PDF at: '${renderResult.value}" }
            is Either.Left -> logger.error { "Unable to generate PDF: ${renderResult.leftOrNull()}" }
        }
    }

    logger.info { "Generated '${filtered.size}' PDF files." }

}

typealias ResumeGenerator = (KLogger, ResumeLocator, TemplateRenderer, PdfConverter, String, GenerateParams) -> Unit
