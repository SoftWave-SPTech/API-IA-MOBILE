package softwave.api_finance_ia.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Finance IA",
                description = "Microservico para geracao e historico de insights financeiros e de gestao com IA",
                version = "1.0.0",
                contact = @Contact(name = "SoftWave")
        )
)
public class OpenApiConfig {
}
