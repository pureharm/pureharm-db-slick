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

package busymachines.pureharm.dbslick

import busymachines.pureharm.db.PureharmDBCoreAliases
import busymachines.pureharm.dbslick.internals._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 12 Jun 2019
  */
@scala.deprecated(
  "Use busymachines.pureharm.dbslick.PureharmSlickPostgresProfile, support for non Postgresql slick will be dropped in the future",
  "0.0.6-M2",
)
trait PureharmSlickDBProfile extends PureharmDBCoreAliases with PureharmDBSlickAliases {
  self: slick.jdbc.JdbcProfile =>

  /** We use this trick to propagate the profile from the top level object to the
    * definition of the "api" object. Otherwise we can't possibly reuse stuff that
    * should make a living there.
    *
    * Thus, in your app you should probably have something like the following:
    *
    * {{{
    *  //you need to depend on a specific support. For Postgres:
    *  //there is the ``pureharm-db-slick-psql` module
    *  import busymachines.pureharm.dbslick.psql.PureharmSlickPostgresProfile
    *
    * trait MyAppSlickProfile
    *   extends PureharmSlickPostgresProfile
    *   /* and all those other imports */ { self =>
    *
    *    override val api: MyAppSlickProfileAPI = new MyAppSlickProfileAPI {}
    *
    *    trait MyAppSlickProfileAPI extends super.API with PureharmSlickPostgresAPIWithImplicits
    * }
    *
    * object MyAppSlickProfile extends MyAppSlickProfile
    * }}}
    *
    * And when you do the following imports:
    * {{{
    *   //this gives you all extra type definitions from pureharm
    *   import MyAppSlickProfile._
    *
    *   //this gives you all important instances provided by pureharm
    *   //like phantomType column types
    *   import MyAppSlickProfile.api._
    * }}}
    *
    * //RECOMMENDED — but not necessary
    *
    * {{{
    *   package myapp
    *
    *   object db extends MyAppSlickProfile {
    *     val implicits: MyAppSlickProfile.MyAppAPI = this.api
    *   }
    * }}}
    *
    * This creates more uniformity in the way you wind up doing imports in pureharm
    * since at calls sites you do:
    * {{{
    *   import myapp.effects._
    *   import myapp.effects.implicits._ //see how to use pureharm-effects-cats
    *   import myapp.db._
    *   import myapp.db.implicits._ //see how to use pureharm-effects-cats
    *
    *   class MyAppDomainSlickDAOIMPL {
    *     //dao method implementations
    *   }
    * }}}
    *
    * Basically, any imports of "pureharm-style" util packages bring in important types. Usually
    * enough to declare APIs and whatnot.
    *
    * While imports of the associated "implicits" brings you everything you need to
    * actually implement things.
    */
  trait PureharmSlickAPIWithImplicits
    extends self.API with PureharmSlickInstances.SproutTypeInstances with SlickConnectionIOCatsInstances
    with PureharmSlickConnectionIOOps.Implicits with SlickRepoQueriesDefinitions with SlickAliases {
    final override protected val enclosingProfile: slick.jdbc.JdbcProfile = self
  }

  final def slickJDBCProfileAPI: SlickJDBCProfileAPI = this.api

  final def jdbcProfileAPI: JDBCProfileAPI = JDBCProfileAPI(slickJDBCProfileAPI)

}
