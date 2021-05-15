import React from 'react';

import {HashRouter, Redirect, Route} from 'react-router-dom'

import AppLayout from './AppLayout'

import DashboardFeature from '../dashboard/DashboardFeature'
import DonationFeature from '../donation/DonationFeature'
import MailchimpFeature from '../mailchimp/MailchimpFeature'
import TransactionFeature from '../transaction/TransactionFeature'

import {MemberFeature} from '@flock-community/flock-eco-feature-member'
import {UserFeature} from '@flock-community/flock-eco-feature-user'

import AppSettings from './AppSettings'
import AppSpinner from './AppSpinner'

import AuthorityUtil from '../utils/AuthorityUtil'

import ApolloClient from 'apollo-boost'
import {ApolloProvider} from '@apollo/react-hooks'


import {createMuiTheme} from '@material-ui/core/styles';

import purple from '@material-ui/core/colors/purple';

const client = new ApolloClient({
  uri: '/graphql',
})

class App extends React.Component {

  state = {
    spinner: true
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

  componentDidMount() {
    fetch('/configuration')
      .then(res => res.json())
      .then(json => {
        if (!json.loggedIn) {
          window.location.replace("/login");
        } else {
          AuthorityUtil.setAuthorities(json.authorities)
          this.setState({
            spinner: false,
            applicationName: json.applicationName
          })
        }

      })
  }

  render() {

    if (this.state.spinner)
      return (<AppSpinner/>)

    return (
      <ApolloProvider client={client}>
        <HashRouter>
          <AppLayout theme={this.theme} title={this.state.applicationName}>

            <Route path='/' exact render={(props) => (
              <Redirect to="/dashboard"/>
            )}/>

            <Route path='/dashboard' exact render={(props) => (
              <DashboardFeature/>
            )}/>

            <Route path='/members' exact render={(props) => (
              <MemberFeature/>
            )}/>

            <Route path='/donations' exact render={(props) => (
              <DonationFeature/>
            )}/>

            <Route path='/transactions/' exact render={(props) => (
              <TransactionFeature/>
            )}/>

            <Route path='/mailchimp' exact render={(props) => (
              <MailchimpFeature/>
            )}/>

            <Route path='/users' exact render={(props) => (
              <UserFeature/>
            )}/>

            <Route path='/settings' render={(props) => (
              <AppSettings/>
            )}/>

          </AppLayout>
        </HashRouter>
      </ApolloProvider>)
  }

}

export default App
