import React from "react";

import Grid from '@material-ui/core/Grid';


import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';

import Button from '@material-ui/core/Button';

import {SelectValidator, ValidatorForm} from 'react-material-ui-form-validator';
import MenuItem from '@material-ui/core/MenuItem';
import Input from '@material-ui/core/Input';

class StopDialog extends React.Component {

  state = {
    reason: ''
  }

  handleClose = () => {
    this.props.onComplete()
  }

  handleChangeEvent = (name) => (ev) => {
    this.setState({[name]: ev.target.value})
  }

  handleSubmit = () => {
    const opts = {
      method: "POST",
      headers: {
        "Content-Type": "application/json; charset=utf-8",
      },
      body: JSON.stringify({
        reason: this.state.reason
      })
    };
    fetch(`/api/donations/${this.props.id}/stop`, opts)
      .then((res) => {
        if (!res.ok) {
          res.json().then(e => {
            this.setState({message: e.message || "Cannot update member"});
          })
        }
        this.props.onComplete();
      })
  }

  componentDidMount() {
    fetch('/api/member_fields')
      .then(res => res.json())
      .then(json => json.find(it => it.name === 'termination_reason'))
      .then(reason => this.setState({reasons: reason.options}))
  }

  render() {
    return (
      <Dialog
        fullWidth
        maxWidth={'xs'}
        open={this.props.open}
        onClose={this.handleClose}>
        <DialogTitle id="simple-dialog-title">Remove mandate</DialogTitle>

        <DialogContent>

          <ValidatorForm id="sepa-download-form" onSubmit={this.handleSubmit}>
            <Grid container spacing={1}>
              <Grid item xs={12}>
                <SelectValidator
                  required
                  fullWidth
                  label={"Reason"}
                  value={this.state.reason || ''}
                  onChange={this.handleChangeEvent("reason")}
                  input={<Input name="age" id="month-label-placeholder"/>}
                  name="month"
                >
                  {this.state.reasons && this.state.reasons
                    .map(it => <MenuItem key={it} value={it}>{it}</MenuItem>)}
                </SelectValidator>

              </Grid>

            </Grid>
          </ValidatorForm>

        </DialogContent>

        <DialogActions>

          <Button type="submit" form="sepa-download-form" color="primary">
            Stop
          </Button>

          <Button onClick={this.handleClose}>
            Cancel
          </Button>

        </DialogActions>

      </Dialog>
    )
  }


}

export default StopDialog;
