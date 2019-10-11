import React from 'react'
import ReactSVG from 'react-svg'
import nl from '../../../../resources/nl-NL.svg'
import en from '../../../../resources/en-GB.svg'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'

const flexContainer = {
    display: 'flex',
    flexDirection: 'row',
    padding: 0,
};

export function DonationLocalization({value, onChange}) {
  const images = [{name: 'en-GB', src: en}, {name: 'nl-NL', src: nl}]

  const Img = ({value}) => {
    return (
      <ReactSVG
        src={value.src}
        beforeInjection={svg => {
          svg.setAttribute('style', 'width: 50px')
        }}
      />
    )
  }

  const handleClick = (value) => (ev) => {
      onChange(value)
  }

  return (
    <List aria-label="country flags" style={flexContainer}>
      {images.map(val => {
        return (
          <ListItem button key={val.name} onClick={handleClick(val)} selected={value && val.name === value.locale}>
            <ListItemIcon>
              <Img value={val} />
            </ListItemIcon>
            <ListItemText primary={val.name} />
          </ListItem>
        )
      })}
    </List>
  )
}
