package com.ong.backend.infrastructure.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Configuração de segurança de ambiente para perfil de produção [SEC-CRED].
 * O BeanFactoryPostProcessor é executado antes da inicialização de qualquer bean
 * (incluindo DataSource e Flyway), garantindo que a aplicação falhe com mensagem clara
 * se a variável de ambiente SPRING_DATASOURCE_PASSWORD não for fornecida.
 */
@Configuration
public class ProductionSecurityConfig {

    @Bean
    @Profile("prod")
    public static BeanFactoryPostProcessor prodPasswordValidator() {
        return beanFactory -> {
            Environment env = beanFactory.getBean(Environment.class);
            String pwd = env.getProperty("SPRING_DATASOURCE_PASSWORD");
            if (pwd == null || pwd.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Configuração inválida de produção [SEC-CRED]: A variável de ambiente SPRING_DATASOURCE_PASSWORD é estritamente obrigatória e não possui valor padrão."
                );
            }
        };
    }
}
