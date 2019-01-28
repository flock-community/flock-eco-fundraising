package community.flock.fundraising.authorities

import community.flock.eco.core.authorities.Authority

enum class DonationsAuthority : Authority {
    READ,
    WRITE,
    DOWNLOAD_SEPA_XML
}