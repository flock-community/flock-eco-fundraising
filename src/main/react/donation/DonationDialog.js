import React from "react";

import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';

import Button from '@material-ui/core/Button';

import Table from '@material-ui/core/Table';
import TableHead from '@material-ui/core/TableHead';
import TableBody from '@material-ui/core/TableBody';
import TableRow from '@material-ui/core/TableRow';
import TableCell from '@material-ui/core/TableCell';

import {ValidatorForm} from 'react-material-ui-form-validator';

import StopDialog from "./StopDialog";


import Chip from '@material-ui/core/Chip';

import PaymentMandateForm from './PaymentMandateForm';
import MemberForm from '@flock-eco/feature-member/src/main/react/member/MemberForm'
import TextField from "@material-ui/core/TextField";
import Grid from "@material-ui/core/Grid";

class DonationDialog extends React.Component {

  state = {
    mandate: null,
    transactions: [],
    stop: false
  }

  componentDidUpdate(prevProps) {

    if (this.props.id !== prevProps.id) {

      if (this.props.id === 0) {
        this.setState({
          mandate: {
            bankAccount: {}
          },
          member: {},
          destination: ''
        });
      } else if (this.props.id) {
        fetch(`/api/donations/${this.props.id}`)
          .then(res => res.json())
          .then(donation => {
            fetch(`/api/payment/mandates/${donation.mandate.id}/transactions`)
              .then(res => res.json())
              .then(transactions => {
                this.setState({
                  donation: donation,
                  member: donation.member,
                  mandate: donation.mandate,
                  transactions: transactions,
                  destination: donation.destination
                });
              })
          })


      } else {
        this.setState({
          donation: null,
          mandate: null,
          member: null,
          transactions: [],
          destination: null
        });
      }

    }
  }

  handleChangeDestination = (ev) => {
    this.setState({destination: ev.target.value})
  }

  handleChangeMandate = (value) => {
    this.setState({mandate: value})
  }

  handleChangeMember = (value) => {
    this.setState({member: value})
  }

  handleClose = () => {
    this.props.onComplete()
  }

  handleStop = () => {
    this.setState({stop: true})
  }

  handleCompleteStop = () => {
    this.setState({stop: false}, () => {
      this.props.onComplete();
    })
  }

  handleSubmit = () => {
    if (this.state.donation && this.state.donation.id) {
      const opts = {
        method: "PUT",
        headers: {
          "Content-Type": "application/json; charset=utf-8",
        },
        body: JSON.stringify({
          member: this.state.member,
          mandate: this.state.mandate,
          destination: this.state.destination
        }),
      };
      fetch(`/api/donations/${this.props.id}`, opts)
        .then((res) => {
          if (!res.ok) {
            res.json().then(e => {
              this.setState({message: e.message || "Cannot update member"});
            })
          }
          this.props.onComplete();
        })
    } else {
      const opts = {
        method: "POST",
        headers: {
          "Content-Type": "application/json; charset=utf-8",
        },
        body: JSON.stringify({
          member: this.state.member,
          mandate: {
            type: "SEPA",
            ...this.state.mandate
          },
          destination: this.state.destination
        }),
      };
      fetch('/api/donations', opts)
        .then((res) => {
          if (!res.ok) {
            res.json().then(e => {
              this.setState({message: e.message || "Cannot create member"});
            })

          }
          this.props.onComplete();
        })
    }
  }

  mandateEnded = () => this.state.donation && this.state.mandate.endDate !== null

  render() {

    if (this.state.mandate === null)
      return null;

    return (
      <React.Fragment>
        <Dialog
          fullWidth
          maxWidth={this.mandateEnded() ? 'sm' : 'md'}
          open={this.state.mandate !== null}
          onClose={this.handleClose}>
          <DialogTitle id="simple-dialog-title">Donation</DialogTitle>

          {this.mandateEnded() ? this.noContent() : this.content()}

          <DialogActions>

            {!this.mandateEnded() && <Button onClick={this.handleStop} color="secondary">
              Stop
            </Button>}

            {!this.mandateEnded() && <Button type="submit" form="donate-form" color="primary">
              Save
            </Button>}

            <Button onClick={this.handleClose}>
              Cancel
            </Button>

          </DialogActions>

        </Dialog>

        <StopDialog
          open={this.state.stop}
          id={this.props.id}
          onComplete={this.handleCompleteStop}/>

      </React.Fragment>

    )
  }

  noContent() {
    return (<DialogContent>
      <p>Mandate ended at: {this.state.mandate.endDate}</p>
      <p>With Reason: {this.state.member.fields['termination_reason']}</p>
    </DialogContent>)
  }

  content() {

    return (
      <React.Fragment>
        <DialogContent>
          <ValidatorForm id="donate-form" onSubmit={this.handleSubmit}>

            {this.state.mandate.frequency !== 'ONCE' &&
            <PaymentMandateForm value={this.state.mandate} onChange={this.handleChangeMandate}/>}
            <Grid container spacing={16}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Destination"
                  value={this.state.destination || ''}
                  onChange={this.handleChangeDestination}
                />
              </Grid>
            </Grid>
            {!this.state.donation && <MemberForm value={this.state.member} onChange={this.handleChangeMember}/>}
          </ValidatorForm>
        </DialogContent>

        {this.state.transactions.length > 0 && <Table>
          <TableHead>
            <TableRow>
              <TableCell>Amount</TableCell>
              <TableCell>Date</TableCell>
              <TableCell style={{textAlign: 'right'}}>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>{this.state.transactions.map(it => {
            return (
              <TableRow
                key={'trans' + it.id}
              >
                <TableCell>&euro; {it.amount},-</TableCell>
                <TableCell>{this.getFormattedDate(it.created)}</TableCell>
                <TableCell style={{textAlign: 'right'}}>
                  {it.status === 'PENDING' ? <Chip label="Pending" style={{
                    backgroundColor: '#ffd805',
                    borderRadius: '0px',
                    width: '80px'
                  }}/> : null}
                  {it.status === 'ERROR' ? <Chip label="Error" style={{
                    backgroundColor: '#ff3366',
                    borderRadius: '0px',
                    width: '80px'
                  }}/> : null}
                  {it.status === 'SUCCESS' ? <Chip label="Success" style={{
                    backgroundColor: '#7ed321',
                    borderRadius: '0px',
                    width: '80px'
                  }}/> : null}
                </TableCell>
              </TableRow>)
          })}
          </TableBody>
        </Table>
        }
      </React.Fragment>

    )
  }


  getFormattedDate(dateStr) {
    if (!dateStr) return;
    const date = new Date(Date.parse(dateStr))
    return date.toDateString();
  }


}

export default DonationDialog;