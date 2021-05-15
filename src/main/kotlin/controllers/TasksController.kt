package community.flock.eco.fundraising.controllers

import community.flock.eco.fundraising.services.GenerateTransactionsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tasks")
class TasksController(
    val generateTransactionsService: GenerateTransactionsService
) {

    @GetMapping("/transactions")
    fun transactions(): ResponseEntity<Unit> {
        generateTransactionsService.run()
        return ResponseEntity.noContent().build()
    }
}
