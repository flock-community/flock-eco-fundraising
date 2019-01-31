package community.flock.eco.fundraising.repositories

import community.flock.eco.fundraising.model.Donation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
interface DonationRepository : PagingAndSortingRepository<Donation, Long>, JpaSpecificationExecutor<Donation> {

    @Query("SELECT distinct don " +
            "FROM Donation don " +
            "LEFT JOIN FETCH don.member mem " +
            "LEFT JOIN FETCH don.mandate man " +
            "LEFT JOIN FETCH mem.groups " +
            "LEFT JOIN FETCH mem.fields ")
    override fun findAll(): List<Donation>

    @Query("SELECT distinct don " +
            "FROM Donation don " +
            "LEFT JOIN FETCH don.member mem " +
            "LEFT JOIN FETCH don.mandate man " +
            "LEFT JOIN FETCH mem.groups " +
            "LEFT JOIN FETCH mem.fields " +
            "WHERE mem.id = ?1")
    fun findByMemberId(id: Long): List<Donation>

    @Query("SELECT distinct don " +
            "FROM Donation don " +
            "LEFT JOIN FETCH don.member mem " +
            "LEFT JOIN FETCH don.mandate man " +
            "LEFT JOIN FETCH mem.groups " +
            "LEFT JOIN FETCH mem.fields " +
            "WHERE man.code = ?1")
    fun findByCode(code: String): Optional<Donation>


    @Query(value = "SELECT distinct don " +
            "FROM Donation don " +
            "LEFT JOIN FETCH don.member mem " +
            "LEFT JOIN FETCH don.mandate man " +
            "LEFT JOIN FETCH mem.groups " +
            "LEFT JOIN FETCH mem.fields " +
            "WHERE mem = NULL " +
            "OR mem.firstName LIKE %?1% " +
            "OR mem.surName LIKE %?1% " +
            "OR mem.email LIKE %?1% " +
            "ORDER BY mem.surName",
            countQuery = "SELECT COUNT(don) " +
                    "FROM Donation don " +
                    "LEFT JOIN don.member mem " +
                    "WHERE don.member = NULL " +
                    "OR mem.firstName LIKE %?1% " +
                    "OR mem.surName LIKE %?1% " +
                    "OR mem.email LIKE %?1% ")
    fun findBySearch(search: String, page: Pageable): Page<Donation>
}


