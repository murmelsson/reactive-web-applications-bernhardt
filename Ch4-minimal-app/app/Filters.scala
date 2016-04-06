
import javax.inject.Inject
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter
import filters.ScoreFilter

class Filters @Inject() (gzip: GzipFilter, score: ScoreFilter) extends HttpFilters {
  val filters = Seq(gzip, SecurityHeadersFilter(), score)
}
