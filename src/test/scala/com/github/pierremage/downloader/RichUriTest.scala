package com.github.pierremage.downloader

import java.net.URI

import org.apache.commons.vfs2.FileSystemException
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class RichUriTest extends FlatSpec with Matchers {

  import com.github.pierremage.downloader.Downloader.RichUri

  "RichUri.fileName" should "return fileName part of URI" in {
    new URI("https://www.google.co.uk/images/srpr/logo11w.png").fileName shouldBe "logo11w.png"
  }

  it should "ignore parameters" in {
    new URI("https://spark.apache.org/images/spark-logo.png?foo=bar&john=doe").fileName shouldBe "spark-logo.png"
  }

  "RichUri.inputStream()" should "return inputStream when scheme is known" in {
    val uri = this.getClass.getResource("/foo.txt").toURI
    Source.fromInputStream(uri.inputStream()).mkString shouldBe "bar"
  }

  it should "throw an exception when scheme is unknown" in {
    intercept[FileSystemException] {
      new URI("git://github.com:rust-lang/rust.git").inputStream()
    }
  }

}
