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

import busymachines.pureharm.db._
import busymachines.pureharm.dbslick._
import busymachines.pureharm.db.testdata._
import busymachines.pureharm.dbslick.testkit._
import busymachines.pureharm.effects._
import busymachines.pureharm.testkit._
import org.typelevel.log4cats.slf4j._

/** @author Daniel Incicau, daniel.incicau@busymachines.com
  * @since 27/01/2020
  */
final class SlickTransactorTest extends PureharmTest {

  implicit override def testLogger: TestLogger = TestLogger(Slf4jLogger.getLogger[IO])

  private val resource = ResourceFixture(testOptions => SlickTransactorTest.transactor(testOptions))

  resource.test("creates the transactor and the session connection is open from the start") {
    implicit trans: Transactor[IO] =>
      for {
        isConnected <- trans.isConnected
      } yield assert(isConnected)
  }

  resource.test("closes the active connection") { implicit trans: Transactor[IO] =>
    for {
      isConnected <- trans.isConnected
      _ = assert(isConnected)
      _           <- trans.closeSession
      isConnected <- trans.isConnected
    } yield assert(!isConnected)
  }

  resource.test("recreates the connection when the current connection is open") { implicit trans: Transactor[IO] =>
    for {
      isConnected <- trans.isConnected
      _ = assert(isConnected)
      _           <- trans.recreateSession
      isConnected <- trans.isConnected
    } yield assert(isConnected)
  }

  resource.test("recreates the connection when the current connection is closed") { implicit trans: Transactor[IO] =>
    for {
      isConnected <- trans.isConnected
      _ = assert(isConnected)
      _           <- trans.closeSession
      isConnected <- trans.isConnected
      _ = assert(!isConnected)
      _           <- trans.recreateSession
      isConnected <- trans.isConnected
    } yield assert(isConnected)
  }

}

private[test] object SlickTransactorTest extends SlickDBTestSetup(testdb.jdbcProfileAPI) {

  override def dbConfig(testOptions: TestOptions)(implicit logger: TestLogger): DBConnectionConfig =
    PHRTestDBConfig.dbConfig.copy(
      schema = PHRTestDBConfig.schemaName(s"slick_trans_${testOptions.location.line}")
    )

}
