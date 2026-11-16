package com.ong.backend.infrastructure.config;

import com.ong.backend.BackendApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Configuração do Banco em Produção [SEC-CRED]")
class ProductionDatabaseConfigurationTest {

    @Test
    @DisplayName("Dado a aplicação em produção sem variável de senha, quando inicia, então falha com mensagem clara [SEC-CRED]")
    void givenProdProfileWithoutPassword_whenContextStarts_thenFailsWithClearMessage() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(BackendApplication.class)
                .profiles("prod")
                .run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SPRING_DATASOURCE_PASSWORD");
    }
}
