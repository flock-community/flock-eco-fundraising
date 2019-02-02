import React from 'react';

import {HashRouter, Route, Redirect} from 'react-router-dom'

import AppLayout from './AppLayout'

import DashboardFeature from '../dashboard/DashboardFeature'
import DonationFeature from '../donation/DonationFeature'
import ReserveFeature from '../reserve/ReserveFeature'
import MailchimpFeature from '../mailchimp/MailchimpFeature'
import ReserveStatistics from '../reserve/ReserveStatistics'
import TransactionFeature from '../transaction/TransactionFeature'

import MemberFeature from 'flock-eco-feature-member/member/MemberFeature'
import UserFeature from 'flock-eco-feature-user/user/UserFeature'

import AppSettings from './AppSettings'
import AppSpinner from './AppSpinner'

import AuthorityUtil from '../utils/AuthorityUtil'

import {createMuiTheme} from '@material-ui/core/styles';

import purple from '@material-ui/core/colors/purple';

class App extends React.Component {

  state = {
    spinner:true
  };

  get theme() {
    return createMuiTheme({
      palette: {
        primary: purple,
        secondary: {
          main: '#f44336',
        },
      },
      typography: {
        useNextVariants: true,
      },
    })
  }

  componentDidMount(){
    fetch('/configuration')
      .then(res => res.json())
      .then(json => {
        if(!json.loggedIn){
          window.location.replace("/oauth2/authorization/google");
        }else{
            AuthorityUtil.setAuthorities(json.authorities)
          this.setState({
            spinner: false,
            applicationName:json.applicationName
          })
        }

      })
  }

  render() {

    if(this.state.spinner)
      return(<AppSpinner/>)

    return (
      <HashRouter>
        <AppLayout theme={this.theme} title={this.state.applicationName}>

          <Route path='/' exact render={(props) => (
            <Redirect to="/dashboard"/>
          )}/>

          <Route path='/dashboard' exact render={(props) => (
            <DashboardFeature />
          )}/>

          <Route path='/members' exact render={(props) => (
            <MemberFeature/>
          )}/>

          <Route path='/donations' exact render={(props) => (
            <DonationFeature/>
          )}/>

          <Route path='/transactions/' exact render={(props) => (
            <TransactionFeature />
          )}/>

          <Route path='/mailchimp' exact render={(props) => (
            <MailchimpFeature/>
          )}/>

          <Route path='/reservations' exact render={(props) => (
            <ReserveFeature/>
          )}/>

          <Route path='/reservations/:id' exact render={(props) => (
            <ReserveStatistics reserve={props.match.params.id}/>
          )}/>

          <Route path='/users' exact render={(props) => (
            <UserFeature/>
          )}/>

          <Route path='/settings' render={(props) => (
            <AppSettings/>
          )}/>

        </AppLayout>
      </HashRouter>
    )
  }

}

export default App