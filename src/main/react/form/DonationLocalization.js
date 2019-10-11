import React from 'react'
import ReactSVG from 'react-svg'
import nl from '../../../../resources/nl-NL.svg'
import en from '../../../../resources/en-GB.svg'

const images = new Map([['en-GB', en], ['nl-NL', nl]])

export function DonationLocalization() {

  return (
      <Fragment>
      images.forEach(value => {
    return (
      <ReactSVG
        src={value}
        beforeInjection={svg => {
          svg.setAttribute('style', 'width: 50px')
        }}
      />
    )
  })
      </Fragment>)
}
