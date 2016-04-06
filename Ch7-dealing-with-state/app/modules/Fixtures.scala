package modules

import javax.inject.Inject
import com.google.inject.AbstractModule
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import play.api.db.Database
import play.api.libs.Crypto

import generated.Tables._    //These come from the output of generateJOOQ sbt-task (uhh...probably)

class Fixtures @Inject() (val crypto: Crypto, val db: Database) extends DatabaseFixtures {
  println("In Fixtures...")
  // We get a transaction from Play's db utility-method:
  db.withTransaction { connection =>
    val sql = DSL.using(connection, SQLDialect.POSTGRES_9_3)

    // Populate 1 user into the user-table in case table has no records:
    if (sql.fetchCount(USER) == 0) {  
      println("User-table empty, so attempting to add new user...")
      val hashedPassword = crypto.sign("nonsecret")

      sql
      .insertInto(USER)
      .columns(
        USER.EMAIL, USER.FIRSTNAME, USER.LASTNAME, USER.PASSWORD
      ).values(
        "bob@marley.org", "Bob", "Marley", hashedPassword
      )

      .execute()
    }
  }
}


trait DatabaseFixtures


class FixturesModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[DatabaseFixtures])
      .to(classOf[Fixtures]).asEagerSingleton
  }
}





