import React from "react";
import {withStyles} from '@material-ui/core/styles';

import Grid from '@material-ui/core/Grid';

import Typography from '@material-ui/core/Typography';

import Card from '@material-ui/core/Card';
import Paper from '@material-ui/core/Paper';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';

import TransactionTable from "./TransactionTable";

import AuthorityUtil from '../utils/AuthorityUtil'

const styles = theme => ({
  tablePaper: {
    marginBottom: 50,
    width: '100%',
    marginTop: theme.spacing(3),
    overflowX: 'auto',
  },
  table: {
    width: 1000,
  },
  button: {
    position: 'fixed',
    right: 20,
    bottom: 20,
    margin: theme.spacing(1),
  }
});

class TransactionFeature extends React.Component {

  data = new Date()
  state = {
    year: this.data.getFullYear(),
    month: this.data.getMonth() + 1,
    reload: true,
  }

  handleDownloadSepa = (ev) => {
    this.setState({sepa: true})
  }

  handleGenerateTransactions = (ev) => {
    fetch(`/tasks/transactions`)
  }

  handleDateClicked = (step) => (ev) => {
    const month = this.state.month + step
    if (month === 0) {
      return this.setState({
        year: this.state.year - 1,
        month: 12
      })
    }
    if (month === 13) {
      return this.setState({
        year: this.state.year + 1,
        month: 1
      })
    } else {
      return this.setState({month})
    }
  }

  handleMarkAllSuccessClick = (item) => {
    const {year, month, reload} = this.state
    const opts = {
      method: "POST",
    }
    fetch(`/api/transactions/${year}/${month}/success`, opts)
      .then(res => {
        this.setState({
          count: parseInt(res.headers.get('x-total')),
          page: parseInt(res.headers.get('x-page'))
        })
        if (res.status === 200) {
          this.setState({reload: !reload});
        }
      })
  }

  handleRowClick = (item) => {
    this.setState({item: item.id})
  }

  handleDownloadSepa = () => {
    const file = `/api/collection/generate/${this.state.year}/${this.state.month}`
    window.open(file, '_blank');
  }

  render() {

    const {classes} = this.props;
    const {year, month, reload} = this.state;

    return (
      <React.Fragment>
        <Grid container spacing={1}>
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Grid container>

                  <Grid item xs={2}>
                    <Typography variant="h5" gutterBottom>
                      {year} - {month}
                    </Typography>
                  </Grid>
                  <Grid item xs={4}>
                    <Button variant="contained" color="primary" onClick={this.handleDateClicked(-1)}>Back</Button>
                    <Button variant="contained" color="primary" onClick={this.handleDateClicked(+1)}>Next</Button>
                  </Grid>
                  <Grid item xs={6} style={{textAlign: 'right'}}>
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={this.handleMarkAllSuccessClick}>Mark Pending as Success</Button>
                    <AuthorityUtil has="DonationsAuthority.DOWNLOAD_SEPA_XML">
                      <Button
                        variant="contained"
                        color="primary"
                        onClick={this.handleDownloadSepa}>Download Sepa Xml</Button>
                    </AuthorityUtil>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12}>
            <Paper className={classes.tablePaper}>
              <TransactionTable
                className={classes.table}
                year={year}
                month={month}
                reload={reload}
                handleRowClick={this.handleRowClick}
              />
            </Paper>
          </Grid>
        </Grid>
      </React.Fragment>
    )
  }
};

export default withStyles(styles)(TransactionFeature);
