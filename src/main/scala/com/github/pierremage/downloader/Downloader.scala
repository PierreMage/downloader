package com.github.pierremage.downloader

import java.io.{File, InputStream}
import java.net.URI
import java.nio.file.Path

import com.github.pierremage._
import org.apache.commons.vfs2.VFS
import org.slf4j.LoggerFactory

import scala.collection.GenSet
import scala.util.{Failure, Success, Try}

object Downloader {

  private val log = LoggerFactory.getLogger(Downloader.getClass)

  /**
   * See <a href="https://commons.apache.org/proper/commons-vfs/filesystems.html">Commons-VFS Supported File Systems</a>
   */
  def download(outputPath: Path, uris: GenSet[URI]): (GenSet[URI], GenSet[Path]) =
    uris.map { uri =>
      val targetPath = outputPath.resolve(uri.fileName)
      Try(copy(uri.inputStream(), targetPath)) match {
        case Failure(e) =>
          log.error(s"Failed to download $uri: ${e.getMessage}")
          Left(uri)
        case Success(_) =>
          Right(targetPath)
      }
    }.partition(_.isLeft) match {
      case (us, ps) => (us.map(_.left.get), ps.map(_.right.get))
    }

  implicit class RichUri(uri: URI) {

    def fileName: String =
      new File(uri.getPath).getName

    def inputStream(): InputStream =
      VFS.getManager.resolveFile(uri.toString).getContent.getInputStream

  }

}
