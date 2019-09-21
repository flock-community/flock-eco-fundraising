package community.flock.eco.fundraising.data

import community.flock.eco.core.authorities.Authority
import community.flock.eco.core.data.LoadData
import community.flock.eco.feature.mailchimp.authorities.MailchimpCampaignAuthority
import community.flock.eco.feature.mailchimp.authorities.MailchimpMemberAuthority
import community.flock.eco.feature.mailchimp.authorities.MailchimpTemplateAuthority
import community.flock.eco.feature.member.authorities.MemberAuthority
import community.flock.eco.feature.member.authorities.MemberFieldAuthority
import community.flock.eco.feature.member.authorities.MemberGroupAuthority
import community.flock.eco.feature.member.model.*
import community.flock.eco.feature.member.repositories.MemberFieldRepository
import community.flock.eco.feature.member.repositories.MemberGroupRepository
import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.payment.authorities.PaymentMandateAuthority
import community.flock.eco.feature.payment.authorities.PaymentTransactionAuthority
import community.flock.eco.feature.payment.model.PaymentBankAccount
import community.flock.eco.feature.payment.model.PaymentFrequency
import community.flock.eco.feature.payment.model.PaymentMandate
import community.flock.eco.feature.payment.model.PaymentType
import community.flock.eco.feature.payment.repositories.PaymentMandateRepository
import community.flock.eco.feature.user.authorities.UserAuthority
import community.flock.eco.feature.user.model.User
import community.flock.eco.feature.user.repositories.UserRepository
import community.flock.eco.fundraising.authorities.DonationsAuthority
import community.flock.eco.fundraising.model.Donation
import community.flock.eco.fundraising.repositories.DonationRepository
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
@ConditionalOnProperty(
        "flock.fundraising.load-data.enabled",
        "flock.fundraising.load-data.excel")
class ExcelLoadData(
        private val userRepository: UserRepository,
        private val donationRepository: DonationRepository,
        private val memberRepository: MemberRepository,
        private val memberFieldRepository: MemberFieldRepository,
        private val memberGroupRepository: MemberGroupRepository,
        private val paymentMandateRepository: PaymentMandateRepository) : LoadData<Member> {

    @Value("\${flock.fundraising.load-data.excel:@null}")
    lateinit var file: String

    val groups: MutableMap<String, MemberGroup> = mutableMapOf()

    override fun load(n: Int): Iterable<Member> {

        if (file == null) return listOf()

        MemberField(
                name = "newsletter",
                label = "Subscribed for newsletter",
                type = MemberFieldType.CHECKBOX
        ).let {
            memberFieldRepository.save(it)
        }

        createUser(
                name = "Willem Veelenturf",
                email = "willem.veelenturf@gmail.com",
                authorities = setOf(
                        UserAuthority.READ,
                        UserAuthority.WRITE,
                        MemberAuthority.READ,
                        MemberAuthority.WRITE,
                        DonationsAuthority.READ,
                        DonationsAuthority.WRITE,
                        PaymentTransactionAuthority.READ,
                        MemberGroupAuthority.READ,
                        MemberFieldAuthority.READ,
                        PaymentMandateAuthority.READ,
                        MailchimpCampaignAuthority.READ,
                        MailchimpTemplateAuthority.READ,
                        MailchimpMemberAuthority.READ
                )
        )

        val file = File(javaClass.classLoader.getResource(file).file)

        return readXlsx(file)
                .asSequence()
                .filterIndexed { index, row -> index > 0 }
                .map {
                    val member = Member(
                            firstName = it.getCell(1).stringCellValue,
                            infix = it.getCell(2)?.let {
                                it.stringCellValue
                            },
                            surName = it.getCell(3).stringCellValue,
                            gender = MemberGender.valueOf(it.getCell(0).stringCellValue),

                            street = it.getCell(6)?.let {
                                when (it.cellTypeEnum) {
                                    CellType.STRING -> it.stringCellValue
                                    else -> null
                                }
                            },
                            houseNumber = it.getCell(7)?.let {
                                it.numericCellValue
                                        .toInt()
                                        .toString()
                            },
                            houseNumberExtension = it.getCell(8)?.let {
                                it.toString()
                            },
                            postalCode = it.getCell(9)?.let {
                                when (it.cellTypeEnum) {
                                    CellType.STRING -> it.stringCellValue
                                    else -> null
                                }
                            },
                            city = it.getCell(10)?.let {
                                it.stringCellValue
                            },
                            country = it.getCell(11)?.let {
                                it.stringCellValue
                            },
                            phoneNumber = it.getCell(12)
                                    ?.let {
                                        it.toString()
                                    }
                                    ?: it.getCell(13)
                                            ?.let {
                                                it.toString()
                                            },
                            email = it.getCell(14)?.let {
                                it.stringCellValue
                            },
                            groups = it.getCell(15)
                                    .let {
                                        this.findMemberGroups(it.stringCellValue)
                                    }
                                    .toSet(),
                            created = it.getCell(5).toLocalDate() ?: LocalDate.now(),
                            status = MemberStatus.ACTIVE
                    ).let {
                        memberRepository.save(it)
                    }


                    if (it.getCell(16) != null && it.getCell(16).cellTypeEnum == CellType.NUMERIC) {
                        createDonation(
                                member = member,
                                paymentBankAccount = PaymentBankAccount(
                                        name = it.getCell(21).stringCellValue,
                                        country = it.getCell(22).stringCellValue,
                                        iban = it.getCell(23).stringCellValue,
                                        bic = it.getCell(24).stringCellValue
                                ),
                                code = it.getCell(16).numericCellValue
                                        .toInt()
                                        .toString(),
                                startDate = it.getCell(17).toLocalDate() ?: LocalDate.now(),
                                endDate = it.getCell(18).toLocalDate(),
                                collectionMonth = it.getCell(28).stringCellValue
                                        .let {
                                            Month.valueOf(it)
                                        },
                                amount = it.getCell(25).numericCellValue,
                                frequency = it.getCell(26).toPaymentFrequency()!!
                        )
                    }
                    member

                }
                .toList()
    }

    private fun readXlsx(file: File): MutableIterator<Row> {
        val excelFile = FileInputStream(file)
        val workbook = XSSFWorkbook(excelFile)
        val sheet = workbook.getSheetAt(0)
        return sheet.iterator()
    }

    private fun createUser(name: String, email: String, authorities: Set<Authority>) {
        User(
                name = name,
                email = email,
                reference = email,
                authorities = authorities
                        .map { it.toName() }
                        .toSet()
        ).let {
            userRepository.save(it)
        }
    }

    fun createDonation(
            member: Member,
            code: String,
            amount: Double,
            frequency: PaymentFrequency,
            startDate: LocalDate,
            endDate: LocalDate?,
            collectionMonth: Month,
            paymentBankAccount: PaymentBankAccount
    ) {

        donationRepository.findByCode(code)
                .map {
                    it.copy(
                            member = member,
                            mandate = PaymentMandate(
                                    amount = amount,
                                    frequency = frequency,
                                    startDate = startDate,
                                    endDate = endDate,
                                    type = PaymentType.SEPA,
                                    bankAccount = paymentBankAccount
                            ).let {
                                paymentMandateRepository.save(it)
                            }

                    )
                }
                .orElseGet {
                    Donation(
                            member = member,
                            mandate = PaymentMandate(
                                    code = code,
                                    amount = amount,
                                    frequency = frequency,
                                    collectionMonth = collectionMonth,
                                    startDate = startDate,
                                    endDate = endDate,
                                    type = PaymentType.SEPA,
                                    bankAccount = paymentBankAccount
                            ).let {
                                paymentMandateRepository.save(it)
                            }

                    )
                }
                .let {
                    donationRepository.save(it)
                }
    }

    fun findMemberGroups(groupString: String): Set<MemberGroup> {
        return groupString
                .split(",")
                .map {
                    groups.getOrPut(it, {
                        memberGroupRepository.findByCode(it)
                                .orElseGet {
                                    MemberGroup(
                                            code = it,
                                            name = it
                                    ).let {
                                        memberGroupRepository.save(it)
                                    }
                                }
                    })

                }
                .toSet()

    }
}

private fun Cell.toLocalDate(): LocalDate? {
    val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
    return when (this.cellTypeEnum) {
        CellType.NUMERIC -> this.dateCellValue.let {
            it.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }
        CellType.STRING -> this.stringCellValue.let {
            LocalDate.parse(it, formatter)
        }
        else -> null
    }
}

private fun Cell.toPaymentFrequency(): PaymentFrequency? {
    return when (this.stringCellValue) {
        "YEARLY" -> PaymentFrequency.YEARLY
        "HALF YEARLY" -> PaymentFrequency.HALF_YEARLY
        "QUARTERLY" -> PaymentFrequency.QUARTERLY
        "MONTHLY" -> PaymentFrequency.MONTHLY
        else -> null
    }

}
