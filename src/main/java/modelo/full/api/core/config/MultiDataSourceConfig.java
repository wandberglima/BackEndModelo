package modelo.full.api.core.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "modelo.full.api.domain.repositories",
        },
        entityManagerFactoryRef = "backModeloEntityManagerFactory",
        transactionManagerRef = "backModeloTransactionManager"
)
@EntityScan(
        basePackages = {
                "modelo.full.api.domain.models",
        }
)
@AllArgsConstructor
public class MultiDataSourceConfig {

    private Environment env;

    // DataSource Primario do Pool
    @Bean(name = "dataSourceBackModelo")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource") // Corrigido para o prefixo correto
    public DataSource BackEndModeloDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("spring.datasource.driver-class-name")));
        dataSource.setJdbcUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));

        // Configurações adicionais do pool
        dataSource.setMaximumPoolSize(5); // Número máximo ajustado para o datasource secundário
        dataSource.setMinimumIdle(2);     // Conexões mínimas inativas
        dataSource.setIdleTimeout(30000); // Tempo máximo para uma conexão ficar inativa
        dataSource.setMaxLifetime(180000); // Tempo máximo de vida da conexão
        dataSource.setConnectionTimeout(30000); // Timeout para obter uma conexão
        return dataSource;
    }

    @Primary
    @Bean(name = "backModeloEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean backModeloEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dataSourceBackModelo") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("modelo.full.api.")
                .persistenceUnit("backend")
                .build();
    }

    @Primary
    @Bean(name = "backModeloTransactionManager")
    public PlatformTransactionManager backModeloTransactionManager(
            @Qualifier("backModeloEntityManagerFactory") EntityManagerFactory backModeloEntityManagerFactory) {
        return new JpaTransactionManager(backModeloEntityManagerFactory);
    }
}
