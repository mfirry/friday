package friday

import cats.effect.{IO, IOApp}
import hardy.HardyClient
import laurel.LaurelClient

object Main extends IOApp.Simple:

  def run: IO[Unit] =
    for
      _ <- IO.println("Fetching upcoming albums from Hardy client...")
      hardyAlbums <- HardyClient.getUpcomingReleasesByDate(
        5,
        23
      ) // example date May 23
      _ <- IO.println(s"Hardy Client returned:\n$hardyAlbums")

      _ <- IO.println("\nFetching upcoming albums from Laurel client...")
      laurelAlbums <- LaurelClient.getUpcomingReleasesByDate(
        5,
        23
      ) // same example date
      _ <- IO.println(s"Laurel Client returned:\n$laurelAlbums")
    yield ()
