package community.flock.eco.fundraising.controllers

import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.payment.model.PaymentType.*
import community.flock.eco.feature.payment.repositories.PaymentMandateRepository
import community.flock.eco.fundraising.model.Donation
import community.flock.eco.fundraising.services.DashboardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    val paymentMandateRepository: PaymentMandateRepository,
    val dashboardService: DashboardService
) {

    data class DashboardModel(
        val totalCollectionValue: Double,
        val totalCollectionValueDestination: Map<String, Double>,
        val totalDonationsOnce: Map<String, Double>,
        val totalMembers: Int,
        val totalMandates: Int,
        val newMembers: List<Member>,
        val newDonationsOnce: List<Donation>,
        val newDonationsRecurring: List<Donation>,
        val totalDonationsDestination: Map<String, Double>,
        val totalDonationsPerYear: Map<String, Double>
    )

    @GetMapping()
    fun index(): DashboardModel {
        val mandates = paymentMandateRepository.findAll().toList()
        return DashboardModel(
            totalCollectionValue = dashboardService.sumTotalCollectionValue(mandates),
            totalCollectionValueDestination = dashboardService.sumTotalCollectionValueByDestination(mandates),
            totalDonationsOnce = dashboardService.countTotalDonations(mandates),
            totalMandates = dashboardService.countMandate(mandates),
            totalMembers = dashboardService.countMembers(),
            newMembers = dashboardService.newMembers(),
            newDonationsOnce = dashboardService.newDonationsOnce(),
            newDonationsRecurring = dashboardService.newDonationsRecurring(),
            totalDonationsDestination = dashboardService.totalDonationsByDestination(),
            totalDonationsPerYear = dashboardService.totalDonationsPerYear()
        )
    }
}
