/*
 * Copyright 2019 BusyMachines
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package busymachines.pureharm.dbslick.test

import busymachines.pureharm.effects._
import busymachines.pureharm.db._
import busymachines.pureharm.db.testdata._
import busymachines.pureharm.db.testkit._
import busymachines.pureharm.dbslick._
import busymachines.pureharm.dbslick.testkit._
import busymachines.pureharm.testkit._
import org.typelevel.log4cats.slf4j._

/** To properly run this test, you probably want to start the
  * PostgreSQL server inside docker using the following script:
  * {{{
  *   ./db/docker-pureharm-postgresql-test.sh
  * }}}
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 12 Jun 2019
  */
final class SlickPHRowRepoTest extends PHRowRepoTest[Transactor[IO]] {

  implicit override def testLogger: TestLogger = TestLogger(Slf4jLogger.getLoggerFromName[IO]("test-logger"))

  override type ResourceType = SlickPHRowRepo[IO]

  override def setup: DBTestSetup[Transactor[IO]] = SlickPHRowRepoTest

  override def resource(testOptions: TestOptions, trans: Transactor[IO]): Resource[IO, SlickPHRowRepo[IO]] =
    Resource.pure[IO, SlickPHRowRepo[IO]] {
      implicit val t:  Transactor[IO] = trans
      implicit val ec: ConnectionIOEC = ConnectionIOEC(runtime.implicitIORuntime.compute)
      SlickPHRowRepo[IO]
    }

  testResource.test("insert row1 + row2 (w/ same unique_string) -> conflict") { implicit repo =>
    for {
      _       <- repo.insert(data.row1)
      attempt <-
        repo
          .insert(data.row2.copy(uniqueString = data.row1.uniqueString))
          .attempt
      failure = interceptFailure[DBUniqueConstraintViolationAnomaly](attempt)
    } yield {
      assert(failure.column == "unique_string", "column name")
      assert(failure.value == UniqueString.oldType(data.row1.uniqueString), "column name")
    }
  }

  testResource.test("insert row1 + row2 (w/ same unique_int) -> conflict") { implicit repo =>
    for {
      _       <- repo.insert(data.row1)
      attempt <-
        repo
          .insert(data.row2.copy(uniqueInt = data.row1.uniqueInt))
          .attempt
      failure = interceptFailure[DBUniqueConstraintViolationAnomaly](attempt)
    } yield {
      assert(failure.column == "unique_int", "column name")
      assert(failure.value == data.row1.uniqueInt.toString, "column name")
    }
  }

  testResource.test("insert row1 + row2 (w/ same unique_json) -> conflict") { implicit repo =>
    import SlickPHRowRepo.pureharmJSONCol
    import busymachines.pureharm.json.implicits._
    for {
      _       <- repo.insert(data.row1)
      attempt <-
        repo
          .insert(data.row2.copy(uniqueJSON = data.row1.uniqueJSON))
          .attempt
      failure = interceptFailure[DBUniqueConstraintViolationAnomaly](attempt)
    } yield {
      assert(failure.column == "unique_json", "column name")
      assertSuccess(failure.value.decodeAs[PHJSONCol])(UniqueJSON.oldType(data.row1.uniqueJSON))
    }
  }
}

private[test] object SlickPHRowRepoTest extends SlickDBTestSetup(testdb.jdbcProfileAPI) {

  override def dbConfig(testOptions: TestOptions)(implicit logger: TestLogger): DBConnectionConfig =
    PHRTestDBConfig.dbConfig.withSchemaFromClassAndTest(prefix = "slick", testOptions = testOptions)

}
