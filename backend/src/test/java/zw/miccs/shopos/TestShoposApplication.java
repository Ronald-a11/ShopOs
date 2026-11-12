package zw.miccs.shopos;

import org.springframework.boot.SpringApplication;

public class TestShoposApplication {

	public static void main(String[] args) {
		SpringApplication.from(ShoposApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
