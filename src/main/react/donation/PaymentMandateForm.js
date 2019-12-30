import React from "react";

import Grid from '@material-ui/core/Grid';

import Input from '@material-ui/core/Input';
import MenuItem from '@material-ui/core/MenuItem';

import PaymentBankAccountForm from './PaymentBankAccountForm';

import {SelectValidator, TextValidator} from 'react-material-ui-form-validator';

class PaymentMandateForm extends React.Component {

  constructor(props) {
    super(props);
    this.state = props.value
  }

  handleChangeEvent = (name) => (event) => {
    this.setState({[name]: event.target.value}, () => {
      this.props.onChange(this.state)
    })
  }

  handleChangeValue = (name) => (value) => {
    this.setState({[name]: value}, () => {
      this.props.onChange(this.state)
    })
  }

  render() {

    return (

      <Grid container spacing={1}>
        <Grid item xs={3}>
          <TextValidator
            required
            fullWidth
            label="Amount"
            name="amount"
            value={this.state.amount || ''}
            onChange={this.handleChangeEvent("amount")}
            validators={['required']}
            errorMessages={['this field is required']}/>
        </Grid>
        <Grid item xs={4}>

          <SelectValidator
            required
            fullWidth
            label={"Frequency"}
            value={this.state.frequency || ''}
            onChange={this.handleChangeEvent("frequency")}
            input={<Input name="age" id="age-label-placeholder"/>}
            name="frequency"
          >
            <MenuItem value="MONTHLY">Monthly</MenuItem>
            <MenuItem value="QUARTERLY">Quarterly</MenuItem>
            <MenuItem value="HALF_YEARLY">Half yearly</MenuItem>
            <MenuItem value="YEARLY">Yearly</MenuItem>
          </SelectValidator>

        </Grid>
        <Grid item xs={5}>
          <SelectValidator
            required
            fullWidth
            label={"Collection month"}
            value={this.state.collectionMonth || ''}
            onChange={this.handleChangeEvent("collectionMonth")}
            input={<Input name="age" id="age-label-collection-month"/>}
            name="collectionMonth"
          >
            <MenuItem value="JANUARY">January</MenuItem>
            <MenuItem value="FEBRUARY">February</MenuItem>
            <MenuItem value="MARCH">March</MenuItem>
            <MenuItem value="APRIL">April</MenuItem>
            <MenuItem value="MAY">May</MenuItem>
            <MenuItem value="JUNE">June</MenuItem>
            <MenuItem value="JULY">July</MenuItem>
            <MenuItem value="AUGUST">August</MenuItem>
            <MenuItem value="SEPTEMBER">September</MenuItem>
            <MenuItem value="OCTOBER">October</MenuItem>
            <MenuItem value="NOVEMBER">November</MenuItem>
            <MenuItem value="DECEMBER">December</MenuItem>

          </SelectValidator>
        </Grid>

        <Grid item xs={12}>

          <PaymentBankAccountForm
            value={this.state.bankAccount}
            onChange={this.handleChangeValue("bankAccount")}/>
        </Grid>
      </Grid>

    )
  }


}

export default PaymentMandateForm;
