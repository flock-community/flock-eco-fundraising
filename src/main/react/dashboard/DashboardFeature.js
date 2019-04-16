import React from "react";
import {withStyles} from '@material-ui/core/styles';

import Grid from '@material-ui/core/Grid';

import Typography from '@material-ui/core/Typography';

import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableRow from '@material-ui/core/TableRow';
import TableCell from '@material-ui/core/TableCell';

import MemberDialog from '@flock-eco/feature-member/src/main/react/member/MemberDialog'

const styles = theme => ({
  card: {
    marginBottom: 10
  },
});

class DonationFeature extends React.Component {

  state = {
    memberId: null,
    memberAction: null,
  }

  money = new Intl.NumberFormat('nl-NL',
    {style: 'currency', currency: 'EUR'}
  )

  componentDidMount() {
    this.loadData()
  }

  loadData() {
    fetch(`/api/dashboard`)
      .then(res => {
        return res.json()
      })
      .then(json => {
        this.setState({data: json});
      })
  }

  handleMemberClick = (it) => (ev) => {
    this.setState({
      memberId: it.id,
      memberAction: 'edit',
    });
  }
  handleComplete = () => {
    this.setState({
      memberId: null,
      memberAction: null,
    });
  }

  render() {

    const {classes} = this.props;

    if (!this.state.data)
      return null

    return (
      <React.Fragment>

        <MemberDialog
          id={this.state.memberId}
          action={this.state.memberAction}
          onComplete={this.handleComplete}/>

        <Grid container spacing={16}>
          <Grid item xs={4}>

            <Card className={classes.card}>
              <CardContent>
                <Typography component="h5" variant="h5">
                  Numbers
                </Typography>
                <Typography component="p" variant="body1">
                  Total number of members: {this.state.data.totalMembers}
                </Typography>
                <Typography component="p" variant="body1">
                  Total number of mandates: {this.state.data.totalMandates}
                </Typography>
              </CardContent>
            </Card>

            <Card className={classes.card}>
              <CardContent>
                <Typography component="h5" variant="h5">
                  Total collection value
                </Typography>
                <Typography component="p" variant="body1">
                  {this.money.format(this.state.data.totalCollectionValue)} per year
                </Typography>
              </CardContent>
            </Card>

            <Card className={classes.card}>
              <CardContent>
                <Typography component="h5" variant="h5">
                  Total once donations
                </Typography>
                {Object.keys(this.state.data.totalDonationsOnce).map(key => {
                  return (<Typography component="p" variant="body1">
                    {key}: {this.money.format(this.state.data.totalDonationsOnce[key])}
                  </Typography>)
                })}
              </CardContent>
            </Card>

            <Card className={classes.card}>
              <CardContent>
                <Typography component="h5" variant="h5">
                  Total donations by destination
                </Typography>
                {Object.keys(this.state.data.totalDonationsDestination).map(key => {
                  return (<Typography component="p" variant="body1">
                    {key}: {this.money.format(this.state.data.totalDonationsDestination[key])}
                  </Typography>)
                })}
              </CardContent>
            </Card>

          </Grid>

          <Grid item xs={8}>
            <Card className={classes.card}>
              <CardContent>
                <Typography component="h5" variant="h5">
                  Members
                </Typography>
              </CardContent>
              <Table>
                <TableBody>
                  {this.state.data.newMembers.map(item => {
                    return (
                      <TableRow key={item.id} onClick={this.handleMemberClick(item)}>
                        <TableCell>{this.toName(item)}</TableCell>
                        <TableCell>{item.created}</TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </Card>

            <Card className={classes.card}>
              <CardContent>
                <Typography component="h5" variant="h5">
                  Donations
                </Typography>
              </CardContent>
              <Table>
                <TableBody>
                  {this.state.data.newDonations.map(item => {
                    return (
                      <TableRow key={item.id} onClick={this.handleMemberClick(item.member)}>
                        <TableCell>{this.toName(item.member)}</TableCell>
                        <TableCell>{this.money.format(item.mandate.amount)}</TableCell>
                        <TableCell>{item.mandate.type}</TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </Card>
          </Grid>


        </Grid>


      </React.Fragment>
    )
  }

  toName(member) {
    if (!member)
      return 'Anonymous'
    return `${member.firstName} ${member.preFix ? member.preFix + ' ' : ''} ${member.surName}`
  }
};

export default withStyles(styles)(DonationFeature);