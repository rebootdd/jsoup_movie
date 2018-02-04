package com.douban.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.douban.movie.web")
public class JsoupDoubanApplication {

	public static void main(String[] args) {
		SpringApplication.run(JsoupDoubanApplication.class, args);
	}
}
