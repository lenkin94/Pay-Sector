package app.paysector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableFeignClients
public class PaySectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaySectorApplication.class, args);
	}

}
