package com.study.til;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
class TilApplicationTests {

	@Test
	void contextLoads() {
		assertThat(4).isEqualTo(4);
	}

}
