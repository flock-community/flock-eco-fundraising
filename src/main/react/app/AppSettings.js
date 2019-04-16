import React from 'react';
import {withStyles} from "@material-ui/core/styles/index";

import {Link, Redirect, Route} from 'react-router-dom'

import Grid from '@material-ui/core/Grid';

import Paper from '@material-ui/core/Paper';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';

import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';

import MemberGroupFeature from '@flock-eco/feature-member/src/main/react/member_group/MemberGroupFeature'
import MemberFieldFeature from '@flock-eco/feature-member/src/main/react/member_field/MemberFieldFeature'

import SystemConfigurationFeature from '../settings/system_configuration/SystemConfigurationFeature'

const styles = {}

class AppSettings extends React.Component {

  render() {
    return (
      <Grid
        container
        spacing={16}
        wrap="nowrap"
      >
        <Grid item xs={3}>
          <Paper>
            <List component="nav">

              <ListItem button>
                <Link to={`/settings/member_groups`}>
                  <ListItemText primary="Member groups"/>
                </Link>
              </ListItem>

              <ListItem button>
                <Link to={`/settings/member_fields`}>
                  <ListItemText primary="Member fields"/>
                </Link>
              </ListItem>

              <ListItem button>
                <Link to={`/settings/system_configuration`}>
                  <ListItemText primary="System configuration"/>
                </Link>
              </ListItem>

            </List>
          </Paper>
        </Grid>

        <Grid item xs={9}>

          <Card>

            <Route path='/settings' exact render={(props) => (
              <Redirect to="/settings/member_groups"/>
            )}/>

            <Route path='/settings/member_groups' exact render={(props) => (
              <MemberGroupFeature/>
            )}/>

            <Route path='/settings/member_fields' exact render={(props) => (
              <MemberFieldFeature/>
            )}/>

            <Route path='/settings/system_configuration' exact render={(props) => (
              <CardContent>
                <SystemConfigurationFeature/>
              </CardContent>
            )}/>

          </Card>
        </Grid>
      </Grid>
    )
  }

}

export default withStyles(styles)(AppSettings);