package com.cibo.scalastan.data

object CsvDataSource {

  private def strip(str: String): String = {
    val str1 = str.trim
    val str2 = if (str1.nonEmpty && str1.head == '\"') str1.tail else str1
    val str3 = if (str2.nonEmpty && str2.last == '\"') str2.dropRight(1) else str2
    str3.trim
  }

  def fromString(content: String, separator: Char = ','): DataSource = {
    val lines = content.split('\n').map(_.split(separator).map(strip))
    val header = lines.head
    val body = lines.tail.toVector
    val len = body.length
    val values = header.zipWithIndex.map { case (name, index) =>
       DataValue(name, Vector(len), body.map(_.apply(index)))
    }
    DataSource(values)
  }

  def fromFile(fileName: String, separator: Char = ','): DataSource = {
    val content = scala.io.Source.fromFile(fileName).getLines.mkString("\n")
    fromString(content, separator)
  }
}
