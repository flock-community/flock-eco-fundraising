package community.flock.eco.fundraising.controllers

import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberStatus
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.member.specifications.MemberSpecification
import community.flock.eco.feature.payment.model.PaymentFrequency
import community.flock.eco.feature.payment.model.PaymentMandate
import community.flock.eco.feature.payment.model.PaymentTransaction
import community.flock.eco.feature.payment.model.PaymentTransactionStatus
import community.flock.eco.feature.payment.model.PaymentType.*
import community.flock.eco.feature.payment.repositories.PaymentMandateRepository
import community.flock.eco.fundraising.model.Donation
import community.flock.eco.fundraising.repositories.DonationRepository
import community.flock.eco.fundraising.services.DashboardService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.persistence.EntityManager

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
        val paymentMandateRepository: PaymentMandateRepository,
        val dashboardService: DashboardService) {

    data class DashboardModel(
            val totalCollectionValue: Int,
            val totalDonationsOnce: Map<String, Double>,
            val totalMembers: Int,
            val totalMandates: Int,
            val newMembers: List<Member>,
            val newDonationsOnce: List<Donation>,
            val newDonationsRecurring: List<Donation>,
            val totalDonationsDestinationOnce: Map<String, Double>,
            val totalDonationsPerYear: Map<String, Double>
    )

    @GetMapping()
    fun index(): DashboardModel {
        val mandates = paymentMandateRepository.findAll().toList()
        return DashboardModel(
                totalCollectionValue = dashboardService.countTotalMandates(mandates),
                totalDonationsOnce = dashboardService.countTotalDonations(mandates),
                totalMandates = dashboardService.countMandate(mandates),
                totalMembers = dashboardService.countMembers(),
                newMembers = dashboardService.newMembers(),
                newDonationsOnce = dashboardService.newDonationsOnce(),
                newDonationsRecurring = dashboardService.newDonationsRecurring(),
                totalDonationsDestinationOnce = dashboardService.totalDonationsDestinationOnce(),
                totalDonationsPerYear = dashboardService.totalDonationsPerYear()
        )
    }



}
