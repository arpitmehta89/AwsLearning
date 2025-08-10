package com.amo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
@EntityScan(basePackages = "com.amo")
@SpringBootApplication
public class S3WorkerMain {

	public static void main(String[] args) {
		SpringApplication.run(S3WorkerMain.class, args);
	}

}
