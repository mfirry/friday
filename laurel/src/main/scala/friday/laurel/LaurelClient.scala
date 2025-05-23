package friday.laurel

import friday.common.*
import cats.effect.*
import sttp.client4.*

import sttp.client4.httpclient.cats.HttpClientCatsBackend
import io.circe.syntax.*
import io.circe.generic.auto.*

object LaurelClient:

  private val baseUrl = "https://www.albumoftheyear.org/upcoming/"
  private val userAgent = "Mozilla/6.0"
  private val pageLimit = 21

  def getUpcomingReleasesByDate(month: Int, day: Int): IO[String] =
    val targetDate = s"${AlbumUtils.mapMonthNumberToName(month)} $day"
    val nextDate = s"${AlbumUtils.mapMonthNumberToName(month)} ${day + 1}"

    HttpClientCatsBackend.resource[IO]().use { backend =>
      def loop(page: Int, acc: List[Album]): IO[List[Album]] =
        if page > pageLimit then IO.pure(acc)
        else
          val pageUrl = if page == 1 then baseUrl else s"$baseUrl$page/"
          val request = basicRequest
            .header("User-Agent", userAgent)
            .get(uri"$pageUrl")

          for
            response <- request.send(backend)
            result <- response.body match
              case Left(error) =>
                IO.println(s"Error fetching page $page: $error") *> IO.pure(acc)
              case Right(html) =>
                val albums = HtmlParser.parseAlbumFromHtml(html)
                val (matching, _) =
                  albums.partition(_.releaseDate == targetDate)
                val stop = albums.exists(_.releaseDate == nextDate)
                if stop then IO.pure(acc ++ matching)
                else loop(page + 1, acc ++ matching)
          yield result

      loop(1, List.empty)
        .map(result => Map("albums" -> result).asJson.noSpaces)
        .handleError(e =>
          AlbumUtils.buildErrorResponse("Releases by date Error", e.getMessage)
        )
    }
