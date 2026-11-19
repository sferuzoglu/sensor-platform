package com.sefa.sensor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "sensor.ingest.udp-port=0")
@Import(TestcontainersConfiguration.class)
class SensorServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
