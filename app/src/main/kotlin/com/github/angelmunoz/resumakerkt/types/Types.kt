package com.github.angelmunoz.resumakerkt.types

import arrow.core.Either
import kotlinx.serialization.Serializable
import org.w3c.dom.Document
import java.io.File

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
  val name: String, val lastName: String, val pitch: String, val email: String
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
  Debug, Trace, Info
}

@JvmInline
value class ResumeError(val error: String)

@JvmInline
value class PdfConvertError(val error: String)

@JvmInline
value class TemplateRenderingError(val error: String)

@JvmInline
value class FileLocatorError(val error: String)

/**
 * A Locator is a function that given a path to a resource.
 * While this could be generalized to "File Locator" here we're only interested in locating resume files
 */
fun interface IResumeLocator {

  /**
   * Given a path to a resume json file,
   * it will return a list of deserialized resume objects inside that file.
   * @param path the path to the resume file
   * @return Either<ResumeError, List<Resume>>
   */
  fun getResume(path: String): Either<ResumeError, List<Resume>>
}

/**
 * Takes a path to a pebble html template in disk (either a default template or an user provided one) and a Resume object
 * it returns the rendered html as a String
 */
fun interface ITemplateRenderer {

  /**
   * For the default implementation if the template starts with '.' and ends with '.html'
   * then the template will be considered as a "custom" template.
   * otherwise it will be taken as the name of a default template provided by the CLI tool
   * @param templateNameOrPath the path to the template file
   * @param payload the resume object to be rendered
   */
  fun render(templateNameOrPath: String, payload: Resume): Either<TemplateRenderingError, String>
}

/**
 * Takes an HTML string and a path to a file where the pdf will be saved to produce a PDF file.
 */
fun interface IPdfConverter {

  /**
   * It will convert the given html string into a pdf file and save it to the given path, if the output path doesn't exist
   * it will try to create the parent directory so it can be created safely.
   * @param html A well-formed HTML document in a string
   * @param outPath the path to the file where the pdf will be saved
   */
  fun convert(html: String, outPath: String): Either<PdfConvertError, String>
}

/**
 * Takes an HTML string and returns a W3C Document
 */
fun interface IDocumentParser {

  /**
   * It will parse the given html string into a W3C Document
   * @param html A well-formed HTML document in a string
   */
  fun parse(html: String): Either<String, Document>
}

/**
 * Takes a path to a file and returns a File object
 */
fun interface IFileLocator {

  /**
   * It will try to find the given path in the filesystem and return a File object
   * @param path the path to the file
   * @param ensureParent if true it will try to create the parent directory of the file if it doesn't exist
   */
  fun find(path: String, ensureParent: Boolean): Either<FileLocatorError, File>
}

/**
 * General top level interface that ties all in together to produce the PDF
 */
fun interface IResumeGenerator {

  /**
   * Given a path to a resume json file, a path to a directory where the pdf will be saved,
   * a template name and a list of languages to be rendered it will produce a PDF file
   * @param resumePath the path to the resume file
   * @param outDir the path to the directory where the pdf will be saved
   * @param template the name of the template to be used
   * @param language the list of languages to be rendered
   */
  fun generate(resumePath: String, outDir: String, template: String, language: List<String>)
}
