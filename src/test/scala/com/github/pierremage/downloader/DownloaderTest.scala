package com.github.pierremage.downloader

import java.io.File
import java.net.{ServerSocket, URI}
import java.nio.file.Files

import org.mockftpserver.fake.filesystem.{FileEntry, UnixFakeFileSystem}
import org.mockftpserver.fake.{FakeFtpServer, UserAccount}
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class DownloaderTest extends FlatSpec with Matchers {

  import Downloader._

  "Downloader" should "download from ftp" in new FtpContext {
    usingFakeFtpServer {
      Downloader.download(outputPath, Set(new URI(s"ftp://john.doe:password@localhost:${fakeFtpServer.getServerControlPort}/bar.txt")))

      outputPath.toFile.list().length shouldBe 1

      val downloadedFile = outputPath.toFile.listFiles()(0)
      downloadedFile.getName shouldBe "bar.txt"
      Source.fromFile(downloadedFile).mkString shouldBe "Lorem ipsum"
    }
  }

  it should "fail silently when ftp credentials are wrong" in new FtpContext {
    usingFakeFtpServer {
      Downloader.download(outputPath, Set(new URI(s"ftp://john.doe:wrong_password@localhost:${fakeFtpServer.getServerControlPort}/bar.txt")))

      outputPath.toFile.list().length shouldBe 0
    }
  }

  it should "fail silently when ftp file doesn't exist" in new FtpContext {
    usingFakeFtpServer {
      Downloader.download(outputPath, Set(new URI(s"ftp://john.doe:password@localhost:${fakeFtpServer.getServerControlPort}/toto.txt")))

      outputPath.toFile.list().length shouldBe 0
    }
  }

  it should "download from http" in {
    val outputPath = Files.createTempDirectory("")
    Downloader.download(outputPath, Set(new URI("http://www.google.com/robots.txt")))

    outputPath.toFile.list().length shouldBe 1
    outputPath.toFile.listFiles()(0).getName shouldBe "robots.txt"
  }

  it should "download from https" in {
    val outputPath = Files.createTempDirectory("")
    Downloader.download(outputPath, Set(new URI("https://www.google.com/robots.txt")))

    outputPath.toFile.list().length shouldBe 1
    outputPath.toFile.listFiles()(0).getName shouldBe "robots.txt"
  }

  class FtpContext {

    def usingFakeFtpServer(f: => Unit) =
      try {
        fakeFtpServer.start()
        f
      } finally {
        fakeFtpServer.stop()
      }

    val outputPath = Files.createTempDirectory("")

    val fakeFtpServer: FakeFtpServer = {

      val homeDirectory = File.createTempFile("ftp", "")

      val fileSystem = new UnixFakeFileSystem
      fileSystem.add(new FileEntry(s"$homeDirectory/bar.txt", "Lorem ipsum"))

      val server = new FakeFtpServer
      server.addUserAccount(new UserAccount("john.doe", "password", homeDirectory.getAbsolutePath))
      server.setFileSystem(fileSystem)
      server.setServerControlPort(freePort)

      server
    }

    def freePort: Int = {
      using(new ServerSocket(0))(_.getLocalPort)
    }
  }

}
