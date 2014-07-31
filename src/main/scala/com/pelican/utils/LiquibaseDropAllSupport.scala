package com.pelican.utils

/**
 * Created by Nightingale on 31.07.2014.
 */
import java.sql.{Connection, SQLException}

import liquibase.exception.LiquibaseException
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.config.{BeanDefinition, BeanFactoryPostProcessor, ConfigurableListableBeanFactory}

import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer

object LiquibaseDropAllSupport {
  private val RUN_BEFORE_BEEN = "liquibase"
}

class LiquibaseDropAllSupport extends SpringLiquibase with BeanFactoryPostProcessor with BeanNameAware {

  private var beanName = ""
  @BeanProperty var dropAllContexts = new ListBuffer[String]()
  @BeanProperty var dropAll = false

  override def afterPropertiesSet() {
    if (allowDrop) {
      dropAllEntities()
    }
    super.afterPropertiesSet()
  }

  def postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    val beanDefinition: BeanDefinition = beanFactory.getBeanDefinition(LiquibaseDropAllSupport.RUN_BEFORE_BEEN)
    beanDefinition.setAttribute("depends-on", beanName)
  }

  override def setBeanName(name: String) { beanName = name }

  protected def allowDrop: Boolean = dropAllContexts.contains(getContexts) || dropAll


  protected def dropAllEntities() {
    try {
      val conn: Connection = getDataSource.getConnection
      conn.prepareCall("DROP SCHEMA public CASCADE;").execute
      conn.prepareCall("CREATE SCHEMA public;").execute
    }
    catch {
      case e: SQLException =>
        throw new LiquibaseException(e)
    }
  }
}