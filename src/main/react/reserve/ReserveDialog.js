import React from "react";

import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';

import Button from '@material-ui/core/Button';

import Grid from '@material-ui/core/Grid';


import {TextValidator, ValidatorForm} from 'react-material-ui-form-validator';

class DonationDialog extends React.Component {

  state = {
    value: null
  }

  componentDidUpdate(prevProps) {
    if (this.props.value !== prevProps.value) {
      this.setState({value: this.props.value});
    }
  }

  handleClose = () => {
    this.props.onComplete();
  }

  handleChange = (name) => (ev) => {
    const value = Object.assign(this.state.value, {[name]: ev.target.value})
    this.setState({value});
  }

  handleSubmit = (ev) => {
    if (this.state.value.id) {
      const opts = {
        method: "PUT",
        headers: {
          "Content-Type": "application/json; charset=utf-8",
        },
        body: JSON.stringify(this.state.value),
      };
      fetch(`/api/reservations/${this.state.value.id}`, opts)
        .then((res) => {
          this.props.onComplete();
        })
    } else {
      const opts = {
        method: "POST",
        headers: {
          "Content-Type": "application/json; charset=utf-8",
        },
        body: JSON.stringify(this.state.value),
      };
      fetch('/api/reservations', opts)
        .then((res) => {
          this.props.onComplete();
        })
    }
  }

  render() {

    const {classes} = this.props;

    if (this.state.value === null)
      return null

    return (
      <Dialog
        fullWidth
        maxWidth={'sm'}
        open={this.state.value !== null}
        onClose={this.handleClose}>
        <DialogTitle id="simple-dialog-title">Donation</DialogTitle>

        <DialogContent>
          <ValidatorForm
            id="reserve-form"
            onSubmit={this.handleSubmit}>
            <Grid
              container
              spacing={16}>

              <Grid item xs={10}>
                <TextValidator
                  required
                  name="name"
                  label="Name"
                  fullWidth
                  value={this.state.value.name || ''}
                  onChange={this.handleChange('name')}
                  validators={['required']}
                  errorMessages={['this field is required']}/>
              </Grid>

              <Grid item xs={10}>
                <TextValidator
                  required
                  name="address"
                  label="Address"
                  fullWidth
                  value={this.state.value.address || ''}
                  onChange={this.handleChange('address')}
                  validators={['required']}
                  errorMessages={['this field is required']}/>
              </Grid>
            </Grid>
          </ValidatorForm>
        </DialogContent>

        <DialogActions>
          <Button onClick={this.handleClose} color="primary">
            Cancel
          </Button>
          <Button type="submit" form="reserve-form" onClick={this.handleSave} color="primary">
            Save
          </Button>
        </DialogActions>

      </Dialog>
    )
  }


  getFormattedDate(dateStr) {
    if (!dateStr) return;
    const date = new Date(Date.parse(dateStr))
    return date.toLocaleString();
  }


}

export default DonationDialog;