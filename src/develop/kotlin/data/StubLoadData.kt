package community.flock.eco.fundraising.data

import community.flock.eco.core.data.LoadData
import community.flock.eco.feature.mailchimp.model.MailchimpMemberStatus
import community.flock.eco.feature.member.develop.data.MemberLoadData
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.payment.data.PaymentLoadData
import community.flock.eco.feature.payment.model.PaymentTransactionStatus
import community.flock.eco.feature.payment.repositories.PaymentTransactionRepository
import community.flock.eco.fundraising.model.Donation
import community.flock.eco.fundraising.repositories.DonationRepository
import community.flock.eco.fundraising.services.GenerateTransactionsService
import community.flock.eco.fundraising.services.MemberFieldService
import community.flock.eco.fundraising.services.MemberFieldService.MemberFields.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty(
    "flock.fundraising.load-data.enabled",
    "flock.fundraising.load-data.stub"
)
class StubLoadData(
    private val memberLoadData: MemberLoadData,
    private val memberRepository: MemberRepository,
    private val paymentLoadData: PaymentLoadData,
    private val paymentTransactionRepository: PaymentTransactionRepository,
    private val donationRepository: DonationRepository,
    private val memberFieldService: MemberFieldService
) : LoadData<Donation> {

    @Autowired
    lateinit var generateTransactionsService: GenerateTransactionsService

    @PostConstruct
    fun init() {

        if (memberRepository.count() > 0) return

        val date = LocalDate
            .now()

        this.load()
        generateTransactionsService.run(date.plusMonths(-2).year, date.plusMonths(-2).month)
        generateTransactionsService.run(date.plusMonths(-1).year, date.plusMonths(-1).month)
        generateTransactionsService.run(date.year, date.month)
        generateTransactionsService.run(date.plusMonths(1).year, date.plusMonths(1).month)
        generateTransactionsService.run(date.plusMonths(2).year, date.plusMonths(2).month)

        val transactions = paymentTransactionRepository.findAll().map { it.copy(status = PaymentTransactionStatus.values().random()) }
        paymentTransactionRepository.saveAll(transactions)
    }

    override fun load(n: Int): Iterable<Donation> {

        memberFieldService.init()
        val memberData = memberLoadData.load(104)
        val paymentMandateData = paymentLoadData.load(34)

        memberData
            .map {
                it.copy(
                    fields = it.fields
                        .plus(NEWSLETTER.key to if (it.id.toInt() % 2 == 0) "true" else "false")
                        .plus(TRANSACTIONAL_MAIL.key to if (it.id.toInt() % 2 == 0) "true" else "false")
                        .plus(MAILCHIMP_STATUS.key to if (it.id.toInt() % 2 == 0) MailchimpMemberStatus.SUBSCRIBED.name else MailchimpMemberStatus.UNSUBSCRIBED.name)
                        .toMutableMap()
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
                    member = member,
                    destination = listOf(null, "456", "789").random()
                )
            }
            .toList()
            .let {
                donationRepository.saveAll(it)
            }
    }
}
