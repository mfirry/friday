package friday.hardy

import cats.effect.*
import friday.common.*
import org.http4s.*
import org.http4s.client.*
import org.http4s.client.middleware.*
import org.http4s.ember.client.*
import org.http4s.headers.*
import org.typelevel.ci.CIString
import io.circe.syntax.*
import io.circe.generic.auto.*

import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jFactory

object HardyClient:

  private val baseUrl = "https://www.albumoftheyear.org/upcoming/"
  private val userAgent = "Mozilla/6.0"
  private val pageLimit = 21

  def getUpcomingReleasesByDate(month: Int, day: Int): IO[String] =
    val targetDate = s"${AlbumUtils.mapMonthNumberToName(month)} $day"
    val nextDate = s"${AlbumUtils.mapMonthNumberToName(month)} ${day + 1}"

    implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]

    EmberClientBuilder.default[IO].build.use { client =>
      def loop(page: Int, acc: List[Album]): IO[List[Album]] =
        if page > pageLimit then IO.pure(acc)
        else
          val pageUrl = if page == 1 then baseUrl else s"$baseUrl$page/"
          val request = Request[IO](
            method = Method.GET,
            uri = Uri.unsafeFromString(pageUrl)
          ).withHeaders(Headers(Header.Raw(CIString("User-Agent"), userAgent)))

          client.run(request).use { resp =>
            if resp.status.isSuccess then
              resp.bodyText.compile.string.flatMap { html =>
                val albums = HtmlParser.parseAlbumFromHtml(html)
                val (matching, _) =
                  albums.partition(_.releaseDate == targetDate)
                val stop = albums.exists(_.releaseDate == nextDate)
                if stop then IO.pure(acc ++ matching)
                else loop(page + 1, acc ++ matching)
              }
            else
              IO.println(s"Failed to fetch $pageUrl: ${resp.status.code}") *> IO
                .pure(acc)
          }

      loop(1, List.empty)
        .map(result => Map("albums" -> result).asJson.noSpaces)
        .handleError(e =>
          AlbumUtils.buildErrorResponse("Releases by date Error", e.getMessage)
        )
    }
