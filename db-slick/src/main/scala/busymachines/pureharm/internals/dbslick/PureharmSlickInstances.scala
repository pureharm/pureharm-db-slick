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

package busymachines.pureharm.internals.dbslick

import scala.reflect.ClassTag

import busymachines.pureharm.sprout._

/** Unfortunately type inference rarely (if ever) works
  * with something fully generic like
  * {{{
  *     trait LowPrioritySproutTypeInstances {
  *     final implicit def genericColumnType[Tag, T](implicit ct: ColumnType[T]): ColumnType[T @@ Tag] =
  *       ct.asInstanceOf[ColumnType[T @@ Tag]]
  *   }
  * }}}
  *
  * At some point in time another crack will have to be taken to implement
  * the fully generic version, but for now giving up...
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 12 Jun 2019
  */
object PureharmSlickInstances {

  trait SproutTypeInstances extends SproutTypeColumnTypes

  trait SproutTypeColumnTypes {
    protected val enclosingProfile: slick.jdbc.JdbcProfile

    import enclosingProfile._

    implicit final def pureharmSproutTypeColumn[Underlying, New: ClassTag](implicit
      newType: NewType[Underlying, New],
      column:  ColumnType[Underlying],
    ): ColumnType[New] = MappedColumnType.base[New, Underlying](newType.oldType, newType.newType)

    implicit final def pureharmSproutRefinedTypeColumn[Underlying, New: ClassTag](implicit
      spook:  RefinedTypeThrow[Underlying, New],
      column: ColumnType[Underlying],
    ): ColumnType[New] = {
      import cats.implicits._
      MappedColumnType.base[New, Underlying](spook.oldType, s => spook.newType[scala.util.Try](s).get)
    }

  }

}
