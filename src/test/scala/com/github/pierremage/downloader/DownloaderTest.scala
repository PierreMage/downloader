package com.github.pierremage.downloader

import java.io.File
import java.net.{ServerSocket, URI}
import java.nio.file.Files

import com.github.pierremage._
import org.mockftpserver.fake.filesystem.{FileEntry, UnixFakeFileSystem}
import org.mockftpserver.fake.{FakeFtpServer, UserAccount}
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class DownloaderTest extends FlatSpec with Matchers {

  "Downloader" should "download from ftp" in new FtpContext {
    usingFakeFtpServer {
      val downloads = Downloader.download(outputPath, Set(new URI(s"ftp://john.doe:password@localhost:${fakeFtpServer.getServerControlPort}/bar.txt")))

      outputPath.toFile.list().length shouldBe 1

      val downloadedFile = outputPath.toFile.listFiles()(0)
      downloadedFile.getName shouldBe "bar.txt"
      Source.fromFile(downloadedFile).mkString shouldBe "Lorem ipsum"
      downloads shouldBe (Set.empty, Set(outputPath.resolve("bar.txt")))
    }
  }

  it should "fail silently when ftp credentials are wrong" in new FtpContext {
    usingFakeFtpServer {
      val uris = Set(new URI(s"ftp://john.doe:wrong_password@localhost:${fakeFtpServer.getServerControlPort}/bar.txt"))

      val downloads = Downloader.download(outputPath, uris)

      outputPath.toFile.list().length shouldBe 0
      downloads shouldBe (uris, Set.empty)
    }
  }

  it should "fail silently when ftp file doesn't exist" in new FtpContext {
    usingFakeFtpServer {
      val uris = Set(new URI(s"ftp://john.doe:password@localhost:${fakeFtpServer.getServerControlPort}/toto.txt"))

      val downloads = Downloader.download(outputPath, uris)

      outputPath.toFile.list().length shouldBe 0
      downloads shouldBe (uris, Set.empty)
    }
  }

  it should "download from http" in {
    val outputPath = Files.createTempDirectory("")

    val downloads = Downloader.download(outputPath, Set(new URI("http://www.google.com/robots.txt")))

    outputPath.toFile.list().length shouldBe 1
    outputPath.toFile.listFiles()(0).getName shouldBe "robots.txt"
    downloads shouldBe (Set.empty, Set(outputPath.resolve("robots.txt")))
  }

  it should "download from https" in {
    val outputPath = Files.createTempDirectory("")

    val downloads = Downloader.download(outputPath, Set(new URI("https://www.google.com/robots.txt")))

    outputPath.toFile.list().length shouldBe 1
    outputPath.toFile.listFiles()(0).getName shouldBe "robots.txt"
    downloads shouldBe (Set.empty, Set(outputPath.resolve("robots.txt")))
  }

  it should "return both failed and successful downloads" in {
    val outputPath = Files.createTempDirectory("")

    val failingUri = new URI("http://www.google.com/bar.txt")
    val downloads = Downloader.download(outputPath, Set(new URI("https://www.google.com/robots.txt"), failingUri))

    outputPath.toFile.list().length shouldBe 1
    outputPath.toFile.listFiles()(0).getName shouldBe "robots.txt"
    downloads shouldBe (Set(failingUri), Set(outputPath.resolve("robots.txt")))
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
