package community.flock.eco.fundraising.controllers

import community.flock.eco.fundraising.services.MonthService
import community.flock.eco.fundraising.services.TransactionSum
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.YearMonth

@RestController
@RequestMapping("/api/month")
class MonthController(
    private val monthService: MonthService
) {

    data class MonthModel(
        val transactionSumByDestinationAndType: List<TransactionSum>,
    )

    @GetMapping()
    fun index(
        @RequestParam yearMonth: YearMonth
    ): MonthModel {
        return MonthModel(
            transactionSumByDestinationAndType = monthService.transactionSumByDestinationAndType(yearMonth),
        )
    }
}
