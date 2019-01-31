package community.flock.eco.fundraising.model

import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.payment.model.PaymentMandate
import javax.persistence.*

@Entity
data class Donation(

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,

        @ManyToOne()
        val member: Member?,

        @ManyToOne()
        val mandate: PaymentMandate

)

