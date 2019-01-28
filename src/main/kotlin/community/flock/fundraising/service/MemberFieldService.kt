package community.flock.fundraising.service

import community.flock.eco.feature.member.model.MemberField
import community.flock.eco.feature.member.model.MemberFieldType
import community.flock.eco.feature.member.repositories.MemberFieldRepository
import community.flock.fundraising.controllers.DonationsController
import org.springframework.stereotype.Service

@Service
class MemberFieldService(
        private val memberFieldRepository: MemberFieldRepository
) {

    private fun isFieldPresent(field: String) = memberFieldRepository.findByName(field).isPresent

    fun init() {

        if (!isFieldPresent(DonationsController.FIELD_NEWSLETTER)) {
            memberFieldRepository.save(MemberField(
                    name = DonationsController.FIELD_NEWSLETTER,
                    label = "Subscribed for newsletter",
                    type = MemberFieldType.CHECKBOX,
                    disabled = true
            ))
        }

        if (!isFieldPresent(DonationsController.FIELD_AGREED_ON_TERMS)) {
            memberFieldRepository.save(MemberField(
                    name = DonationsController.FIELD_AGREED_ON_TERMS,
                    label = "Agreed on terms",
                    type = MemberFieldType.CHECKBOX,
                    disabled = true
            ))
        }

        if (!isFieldPresent(DonationsController.TERMINATION_REASON)) {
            memberFieldRepository.save(MemberField(
                    name = DonationsController.TERMINATION_REASON,
                    label = "Termination reason",
                    type = MemberFieldType.SINGLE_SELECT,
                    disabled = true,
                    options = sortedSetOf(
                            "Reason 1",
                            "Reason 2",
                            "Reason 3")
            ))
        }
    }
}