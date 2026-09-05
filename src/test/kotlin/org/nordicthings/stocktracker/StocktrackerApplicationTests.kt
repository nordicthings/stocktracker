package org.nordicthings.stocktracker

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:stocktracker-test;MODE=MariaDB;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    ],
)
class StocktrackerApplicationTests {

    @Test
    fun contextLoads() {
    }
}
