package es.upm.dit.isst.tfgapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "es.upm.dit.isst.tfgapi")
public class TFGApplication {

	public static void main(String[] args) {
		SpringApplication.run(TFGApplication.class, args);
	}

}
