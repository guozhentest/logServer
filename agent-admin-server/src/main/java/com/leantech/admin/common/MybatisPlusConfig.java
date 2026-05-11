package com.leantech.admin.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;

@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Primary
    @Bean(name = "adminDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.admin")
    public DataSource adminDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "centerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.center")
    public DataSource centerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "adminSqlSessionFactory")
    public SqlSessionFactory adminSqlSessionFactory(@Qualifier("adminDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        return factory.getObject();
    }

    @Bean(name = "centerSqlSessionFactory")
    public SqlSessionFactory centerSqlSessionFactory(@Qualifier("centerDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        return factory.getObject();
    }

    @Primary
    @Bean(name = "adminTransactionManager")
    public DataSourceTransactionManager adminTransactionManager(@Qualifier("adminDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "centerTransactionManager")
    public DataSourceTransactionManager centerTransactionManager(@Qualifier("centerDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = "adminSqlSessionTemplate")
    public SqlSessionTemplate adminSqlSessionTemplate(@Qualifier("adminSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "centerSqlSessionTemplate")
    public SqlSessionTemplate centerSqlSessionTemplate(@Qualifier("centerSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Configuration
    @MapperScan(basePackages = "com.leantech.admin.system.mapper", sqlSessionTemplateRef = "adminSqlSessionTemplate")
    public static class AdminMapperConfig {
    }

    @Configuration
    @MapperScan(basePackages = "com.leantech.admin.agent.mapper", sqlSessionTemplateRef = "centerSqlSessionTemplate")
    public static class CenterMapperConfig {
    }
}
