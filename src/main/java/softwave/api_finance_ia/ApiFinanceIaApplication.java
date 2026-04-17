package softwave.api_finance_ia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "softwave.api_finance_ia.feign")
public class ApiFinanceIaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiFinanceIaApplication.class, args);
    }
}
