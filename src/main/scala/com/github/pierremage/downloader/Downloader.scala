package com.github.pierremage.downloader

import java.io.{File, InputStream}
import java.net.URI
import java.nio.file.{CopyOption, Files, Path}

import org.apache.commons.vfs2.VFS
import org.slf4j.LoggerFactory

import scala.collection.GenSet
import scala.util.{Failure, Try}

object Downloader {

  private val log = LoggerFactory.getLogger(Downloader.getClass)

  /**
   * See <a href="https://commons.apache.org/proper/commons-vfs/filesystems.html">Commons-VFS Supported File Systems</a>
   */
  def download(outputPath: Path, uris: GenSet[URI]): Unit =
    uris.foreach { uri =>
      Try(copy(uri.inputStream(), outputPath.resolve(uri.fileName))) match {
        case Failure(e) =>
          log.error(s"Failed to download $uri: ${e.getMessage}")
        case _ =>
          log.debug(s"Downloaded {}", uri)
      }
    }

  def copy(in: => InputStream, target: Path, options: CopyOption*): Long =
    using(in)(Files.copy(_, target, options: _*))

  def using[A <: AutoCloseable, B](a: A)(f: A => B): B =
    try {
      f(a)
    } finally {
      a.close()
    }

  implicit class RichUri(uri: URI) {

    def fileName: String =
      new File(uri.getPath).getName

    def inputStream(): InputStream =
      VFS.getManager.resolveFile(uri.toString).getContent.getInputStream

  }

}
