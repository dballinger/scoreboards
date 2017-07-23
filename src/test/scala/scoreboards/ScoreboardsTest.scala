package scoreboards

import com.google.common.base.Predicate
import org.openqa.selenium.{WebDriver, By}
import org.openqa.selenium.support.ui.WebDriverWait
import org.scalatest.{FlatSpec, Matchers}

class ScoreboardsTest extends FlatSpec with Matchers {

  import scoreboards.Scoreboards._

  "Scoreboards" should "orchestrate the data collection" in new BaseFixture {
    val scoreboards = new Scoreboards(columnNamesResolver, pageDataResolver, nextPage, sorter, collector)
    scoreboards.collect()

    actualNames shouldBe List("a", "b", "c")
    actualData shouldBe indexColumns(List(
      List("1", "a", "3"),
      List("10", "b", "12"),
      List("7", "c", "9"),
      List("4", "d", "6")
    ))(actualNames)
  }

  it should "use a web driver with javascript capabilities capabilities" in new JsNavFixture {
    val driverCheckingNextPage: NextPage = {
      driver =>
        loadPageWithJs(driver)
        triggerJsActionToCreateElement(driver)
        elementHasBeenCreatedByJavascript(driver) shouldBe true
        None
    }
    val scoreboards = new Scoreboards(columnNamesResolver, pageDataResolver, driverCheckingNextPage, sorter, collector)
    scoreboards.collect()
  }

  trait BaseFixture {
    val sortColumn = 1
    val pageSize = 2
    val numOfPages = 2
    var currentPage = -1

    var actualNames: ColumnNames = _
    var actualData: PageData = _

    val columnNames = List("a", "b", "c")
    val columnNamesResolver: ColumnNamesResolver = { driver => columnNames }

    val pageDataResolver: PageDataResolver = {
      driver =>
        val pageData = indexColumns(
          List(
            List("1", "a", "3"),
            List("4", "d", "6"),
            List("7", "c", "9"),
            List("10", "b", "12")
          )

        )(columnNames)

        pageData.slice(
          currentPage * pageSize,
          currentPage * pageSize + currentPage + pageSize
        )
    }

    val nextPage: NextPage = {
      driver =>
        if (currentPage + 1 < numOfPages) {
          currentPage = currentPage + 1
          Some(driver)
        } else None
    }

    val sorter: Sorter = {
      pageData =>
        pageData.sortBy(_ (columnNames(sortColumn)))
    }

    val collector: Collector = {
      (names, data) =>
        actualNames = names
        actualData = data
    }
  }

  trait JsNavFixture extends BaseFixture {
    def elementHasBeenCreatedByJavascript(driver: WebDriver): Boolean = !driver.findElements(By.cssSelector("#container span")).isEmpty

    def loadPageWithJs(driver: WebDriver): Unit = driver.get(getClass.getResource("/js-nav.html").toString)

    def triggerJsActionToCreateElement(driver: WebDriver): Unit = {
      driver.findElement(By.tagName("button")).click()
      new WebDriverWait(driver, 1).until(new Predicate[WebDriver] {
        override def apply(input: WebDriver): Boolean = elementHasBeenCreatedByJavascript(input)
      })
    }
  }

  val indexColumns: List[List[String]] => List[String] => List[Map[String, String]] = {
    rows: List[List[String]] =>
      columnNames: List[String] =>
        for {
          row <- rows
        } yield columnNames.zip(row).toMap
  }
}