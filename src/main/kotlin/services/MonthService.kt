package community.flock.eco.fundraising.services

import community.flock.eco.feature.member.repositories.MemberRepository
import community.flock.eco.feature.payment.model.PaymentTransactionStatus
import community.flock.eco.feature.payment.model.PaymentType
import community.flock.eco.fundraising.repositories.DonationRepository
import org.springframework.stereotype.Service
import java.time.YearMonth
import javax.persistence.EntityManager

data class TransactionSum(
        val type: PaymentType,
        val destination: String,
        val total: Double
)

@Service
class MonthService(
        private val entityManager: EntityManager,
) {
    fun transactionSumByDestinationAndType(yearMonth: YearMonth): List<TransactionSum> {
        val query = """
            SELECT 
                d.mandate.type, 
                UPPER(d.destination), 
                SUM(t.amount) 
            FROM Donation d
                JOIN d.mandate.transactions t
            WHERE t.status = :status
            AND t.created BETWEEN :from AND :till
            GROUP BY d.mandate.type, UPPER(d.destination)
            
        """
        return entityManager
                .createQuery(query)
                .setParameter("status", PaymentTransactionStatus.SUCCESS)
                .setParameter("from", yearMonth.atDay(1))
                .setParameter("till", yearMonth.atEndOfMonth())
                .resultList
                .map { it as Array<*> }
                .map {
                    TransactionSum(
                            type = it[0] as PaymentType,
                            destination = it[1]?.toString() ?: "UNKNOWN",
                            total = it[2] as Double
                    )
                }
    }


}
