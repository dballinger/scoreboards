package scoreboards

import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities
import scoreboards.Scoreboards._

import scala.annotation.tailrec

class Scoreboards(columnNameResolver: ColumnNamesResolver,
                  pageDataResolver: PageDataResolver,
                  nextPage: NextPage,
                  sort: Sorter,
                  collector: Collector) {

  def collect(): Unit = {
    @tailrec def extract(data: PageData, driver: Option[WebDriver]): PageData = driver match {
      case None => data
      case Some(d) => extract(data ::: pageDataResolver(d), nextPage(d))
    }

    val driver = nextPage(new HtmlUnitDriver(DesiredCapabilities.firefox()))
    val pageData = extract(Nil, driver)
    val columnNames = driver map columnNameResolver

    collector(columnNames.getOrElse(Nil), sort(pageData))
  }
}

object Scoreboards {
  type ColumnNames = List[String]
  type PageData = List[Row]
  type Row = List[String]
  type ColumnNamesResolver = (WebDriver) => ColumnNames
  type PageDataResolver = (WebDriver) => PageData
  type NextPage = (WebDriver) => Option[WebDriver]
  type Sorter = PageData => PageData
  type Collector = (ColumnNames, PageData) => Unit
}