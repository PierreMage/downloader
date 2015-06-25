package com.github

import java.io.InputStream
import java.nio.file.{Files, CopyOption, Path}

package object pierremage {

  def copy(in: => InputStream, target: Path, options: CopyOption*): Long =
    using(in)(Files.copy(_, target, options: _*))

  def using[A <: AutoCloseable, B](a: A)(f: A => B): B =
    try {
      f(a)
    } finally {
      a.close()
    }

}
