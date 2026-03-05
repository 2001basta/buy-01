package com.example.ApiGateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.cloud.gateway.routes[0].id=test",
    "spring.cloud.gateway.routes[0].uri=http://localhost",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**"
})

class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
