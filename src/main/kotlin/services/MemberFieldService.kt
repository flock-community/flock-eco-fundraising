package community.flock.eco.fundraising.services

import community.flock.eco.feature.member.model.Member
import community.flock.eco.feature.member.model.MemberField
import community.flock.eco.feature.member.model.MemberFieldType
import community.flock.eco.feature.member.repositories.MemberFieldRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class MemberFieldService(
        private val memberFieldRepository: MemberFieldRepository
) {

    enum class MemberFields(val key: String, val label: String, val type: MemberFieldType, val disabled: Boolean, val options: SortedSet<String> = sortedSetOf()) {
        AGREED_ON_TERMS("agreed_on_terms", "Agreed on terms", MemberFieldType.CHECKBOX, true),
        NEWSLETTER("newsletter", "Newsletter", MemberFieldType.CHECKBOX, true),
        TRANSACTIONAL_MAIL("transactional_email", "Transactional email", MemberFieldType.CHECKBOX, true),
        MAILCHIMP_STATUS("mailchimp_status", "Mailchimp status", MemberFieldType.SINGLE_SELECT, true, sortedSetOf("SUBSCRIBED", "UNSUBSCRIBED", "CLEANED", "PENDING","TRANSACTIONAL")),
        TERMINATION_REASON("termination_reason", "Termination reason", MemberFieldType.SINGLE_SELECT, true, sortedSetOf("Reason 1", "Reason 2", "Reason 3"))
    }

    fun init() {
        MemberFields.values()
                .filter { !memberFieldRepository.findByName(it.key).isPresent }
                .map {
                    MemberField(
                            name = it.key,
                            label = it.label,
                            type = it.type,
                            disabled = it.disabled,
                            options = it.options
                    )
                }
                .let { memberFieldRepository.saveAll(it) }
    }

}

fun Member.getFieldValue(field: MemberFieldService.MemberFields) = this.fields[field.key]
