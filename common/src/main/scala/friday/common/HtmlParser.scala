package friday.common

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import scala.jdk.CollectionConverters.*
import io.circe.generic.auto.*
import io.circe.syntax.*

object HtmlParser:

  def parseAlbumFromHtml(html: String): List[Album] =
    val doc: Document = Jsoup.parse(html)
    val albums = doc.select("div.albumBlock.five.small").asScala.toList
    albums.flatMap(parseAlbumDiv)

  private def parseAlbumDiv(div: Element): Option[Album] =
    val artist = Option(div.selectFirst("div.artistTitle")).map(_.text())
    val title = Option(div.selectFirst("div.albumTitle")).map(_.text())
    val date = Option(div.selectFirst("div.type")).map(_.text())

    for
      a <- artist
      t <- title
      d <- date
    yield Album(t, a, d)

object AlbumUtils:

  def mapMonthNumberToName(month: Int): String =
    val monthNames = List(
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec"
    )
    monthNames
      .lift(month - 1)
      .getOrElse(
        throw new IllegalArgumentException(s"Invalid month number: $month")
      )

  def buildErrorResponse(errorType: String, msg: String): String =
    val error = Map("error" -> errorType, "message" -> msg)
    error.asJson.noSpaces
