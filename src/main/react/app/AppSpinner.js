import React from 'react';

import {withStyles} from "@material-ui/core/styles/index";

import Grid from '@material-ui/core/Grid';
import CircularProgress from '@material-ui/core/CircularProgress';

const styles = theme => ({
  root: {
    width: '100%',
    display: 'flex',
  },
});

class AppSpinner extends React.Component {

  render() {
    const {classes} = this.props;

    return (<Grid
      container
      justify="center"
      alignItems="center"
      style={{height: '100%'}}
    >
      <Grid item><CircularProgress/></Grid>
    </Grid>)
  }

}

export default withStyles(styles)(AppSpinner);