/*
 * Copyright 2020 Typelevel
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

package cats.effect
package laws

import cats.{Eq, Eval, Show}
import cats.free.FreeT
import cats.laws.discipline.arbitrary._
import cats.effect.testkit.{freeEval, FreeSyncGenerators, SyncTypeGenerators}
import cats.effect.testkit.FreeSyncEq
import freeEval.{syncForFreeT, FreeEitherSync}
import cats.syntax.all._

import org.scalacheck.Prop
import org.scalacheck.util.Pretty

import org.specs2.ScalaCheck
import org.specs2.mutable._

import org.typelevel.discipline.specs2.mutable.Discipline

class FreeSyncSpec
    extends Specification
    with Discipline
    with ScalaCheck
    with BaseSpec
    with LowPriorityImplicits {
  import FreeSyncGenerators._
  import SyncTypeGenerators._

  implicit def prettyFromShow[A: Show](a: A): Pretty =
    Pretty.prettyString(a.show)

  implicit val eqThrowable: Eq[Throwable] =
    Eq.fromUniversalEquals

  implicit def exec(sbool: FreeEitherSync[Boolean]): Prop =
    run(sbool).fold(Prop.exception(_), b => if (b) Prop.proved else Prop.falsified)

  implicit val scala_2_12_is_buggy
      : Eq[FreeT[Eval, Either[Throwable, *], Either[Int, Either[Throwable, Int]]]] =
    eqFreeSync[Either[Throwable, *], Either[Int, Either[Throwable, Int]]]

  checkAll("FreeEitherSync", SyncTests[FreeEitherSync].sync[Int, Int, Int])
}

//See the explicitly summoned implicits above - scala 2.12 has weird divergent implicit expansion problems
trait LowPriorityImplicits extends FreeSyncEq {}
