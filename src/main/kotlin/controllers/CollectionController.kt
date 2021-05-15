package community.flock.eco.fundraising.controllers

import community.flock.eco.feature.payment.model.PaymentTransaction
import community.flock.eco.feature.payment.model.PaymentTransactionStatus
import community.flock.eco.feature.payment.model.PaymentType
import community.flock.eco.feature.payment.repositories.PaymentTransactionRepository
import community.flock.eco.feature.payment.services.PaymentSepaXmlService
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.StringWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@RestController
@RequestMapping("/api/collection/")
class CollectionController(
    private val paymentTransactionRepository: PaymentTransactionRepository,
    private val paymentSepaXmlService: PaymentSepaXmlService
) {

    @Value("\${flock.fundraising.donations.sepa.privateIdentification:@null}")
    lateinit var sepaPrivateIdentification: String

    @Value("\${flock.fundraising.donations.sepa.message:@null}")
    lateinit var sepaMessage: String

    @Value("\${flock.fundraising.donations.sepa.name:@null}")
    lateinit var sepaName: String

    @Value("\${flock.fundraising.donations.sepa.iban:@null}")
    lateinit var sepaIban: String

    @Value("\${flock.fundraising.donations.sepa.bic:@null}")
    lateinit var sepaBic: String

    @Value("\${flock.fundraising.donations.sepa.address1:@null}")
    lateinit var sepaAddress1: String

    @Value("\${flock.fundraising.donations.sepa.address2:@null}")
    lateinit var sepaAddress2: String

    @Value("\${flock.fundraising.donations.sepa.country:@null}")
    lateinit var sepaCountry: String

    @Value("\${flock.fundraising.donations.collection.dayOfMonth:1}")
    lateinit var dayOfMonth: String

    @GetMapping("/generate/{year}/{month}", produces = [MediaType.APPLICATION_XML_VALUE])
    fun generate(@PathVariable year: Int, @PathVariable month: Int): String {
        val date = LocalDate.of(year, month, 1)

        val startDate = date.with(TemporalAdjusters.firstDayOfMonth())
        val endDate = date.with(TemporalAdjusters.lastDayOfMonth())
        val transactions = paymentTransactionRepository
            .findBetweenDate(startDate, endDate)
            .filter { it.status == PaymentTransactionStatus.PENDING }
            .filter { it.mandate.type == PaymentType.SEPA }

        val sepa = PaymentSepaXmlService.Sepa(
            id = hash(transactions),
            privateIdentification = sepaPrivateIdentification,
            message = sepaMessage,
            organisation = PaymentSepaXmlService.SepaOrganisation(
                name = sepaName,
                iban = sepaIban.replace(" ", ""),
                bic = sepaBic.replace(" ", ""),
                address1 = sepaAddress1,
                address2 = sepaAddress2,
                country = PaymentSepaXmlService.SepaCountry.valueOf(sepaCountry)
            ),
            collectionDateTime = LocalDateTime.now()
                .withDayOfMonth(dayOfMonth.toInt()),
            transactions = transactions
        )

        val doc = paymentSepaXmlService.generate(sepa)

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()

        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.buffer.toString()
    }

    private fun hash(transactions: List<PaymentTransaction>): String {
        return transactions
            .sumBy { it.hashCode() }
            .let {
                DigestUtils.sha256Hex(it.toString())
            }
            .let {
                it.substring(0, 32)
            }
    }
}
