package org.nordicthings.stocktracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StocktrackerApplication

fun main(args: Array<String>) {
    runApplication<StocktrackerApplication>(*args)
}
