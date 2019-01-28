import React from 'react';

import {withStyles} from '@material-ui/core/styles';

const styles = theme => ({});

class AuthorityUtil extends React.Component {

  static authorities = null;

  static setAuthorities(authorities){
    AuthorityUtil.authorities = authorities
  }

  render() {

    if(!AuthorityUtil.authorities){
      return null
    }

    const hasAuthority = this.props.has.split(',')
      .map(it => AuthorityUtil.authorities.includes(it))
      .reduce((acc, cur) => acc ? acc : cur, false)
    return hasAuthority ? this.props.children: null
  }

}

export default withStyles(styles)(AuthorityUtil);