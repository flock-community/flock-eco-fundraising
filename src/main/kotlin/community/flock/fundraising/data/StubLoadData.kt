package community.flock.fundraising.data

import community.flock.eco.core.data.LoadData
import community.flock.eco.feature.member.data.MemberLoadData
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.payment.data.PaymentLoadData
import community.flock.fundraising.model.Donation
import community.flock.fundraising.repositories.DonationRepository
import community.flock.fundraising.service.GenerateTransactionsService
import community.flock.fundraising.service.MemberFieldService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty(
        "flock.fundraising.load-data.enabled",
        "flock.fundraising.load-data.stub")
class StubLoadData(
        private val memberLoadData: MemberLoadData,
        private val memberRepository: MemberRepository,
        private val paymentLoadData: PaymentLoadData,
        private val donationRepository: DonationRepository,
        private val memberFieldService: MemberFieldService
) : LoadData<Donation> {

    @Autowired
    lateinit var generateTransactionsService: GenerateTransactionsService

    @PostConstruct
    fun init() {
        val date = LocalDate
                .now()

        this.load()
        generateTransactionsService.run(date.year, date.month)
        generateTransactionsService.run(date.plusMonths(1).year, date.plusMonths(1).month)
        generateTransactionsService.run(date.plusMonths(2).year, date.plusMonths(2).month)
    }

    override fun load(n: Int): Iterable<Donation> {

        memberFieldService.init()
        val memberData = memberLoadData.load(104)
        val paymentMandateData = paymentLoadData.load(34)

        memberData
                .map {
                    it.copy(
                            fields = it.fields
                                    .plus(
                                            "newsletter" to if (it.id.toInt() % 2 == 0) "true" else "false"
                                    )
                    )
                }
                .toList()
                .let {
                    memberRepository.saveAll(it)
                }

        return memberData
                .mapIndexed { i, member ->
                    Donation(
                            mandate = paymentMandateData.toList()[i],
                            member = member
                    )
                }
                .toList()
                .let {
                    donationRepository.saveAll(it)
                }
    }
}