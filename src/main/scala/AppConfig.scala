/**
 * Created by Nightingale on 31.07.2014.
 */

import javax.sql.DataSource

import com.pelican.utils.LiquibaseDropAllSupport
import com.typesafe.config.ConfigFactory
import org.apache.commons.dbcp.BasicDataSource


class AppConfig {
  val env = scala.util.Properties.propOrElse("spring.profiles.active", scala.util.Properties.envOrElse("ENV", "test"))
  val conf = ConfigFactory.load()
  val default = conf.getConfig("habr.template.default")
  val config = conf.getConfig("habr.template." + env).withFallback(default)

  def dataSource = {
    val ds = new BasicDataSource
    ds.setDriverClassName("org.postgresql.Driver")
    ds.setUsername(config.getString("db.user"))
    ds.setPassword(config.getString("db.password"))
    ds.setMaxActive(20)
    ds.setMaxIdle(10)
    ds.setInitialSize(10)
    ds.setUrl(config.getString("db.url"))
    ds
  }

  def liquibase(dataSource: DataSource) = {
    val liquibase = new LiquibaseDropAllSupport()
    liquibase.setDataSource(dataSource)
    liquibase.setChangeLog("classpath:changelog/db.changelog-master.xml")
    liquibase.setContexts(env)
    liquibase.setShouldRun(true)
    liquibase.dropAllContexts += "test"
    liquibase
  }

}
