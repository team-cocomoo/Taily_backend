package com.cocomoo.taily;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TailyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TailyApplication.class, args);
	}

}
