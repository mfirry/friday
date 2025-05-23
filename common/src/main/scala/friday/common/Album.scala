package friday.common
import io.circe.generic.auto._

final case class Album(name: String, artist: String, releaseDate: String)
