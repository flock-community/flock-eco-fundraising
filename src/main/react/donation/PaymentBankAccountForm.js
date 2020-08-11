import React from "react";

import Grid from '@material-ui/core/Grid';

import Input from '@material-ui/core/Input';
import MenuItem from '@material-ui/core/MenuItem';

import {SelectValidator, TextValidator} from 'react-material-ui-form-validator';

class PaymentBankAccountForm extends React.Component {

  constructor(props) {
    super(props);
    this.state = props.value
  }

  handleChange = (name) => (event) => {
    this.setState({[name]: event.target.value}, () => {
      this.props.onChange(this.state)
    })
  }

  render() {

    return (

      <Grid container spacing={1}>

        <Grid item xs={4}>
          <TextValidator
            required
            fullWidth
            label="Name"
            name="name"
            value={this.state.name || ''}
            onChange={this.handleChange("name")}
            validators={['required']}
            errorMessages={['this field is required']}/>
        </Grid>

        <Grid item xs={3}>
          <TextValidator
            required
            fullWidth
            label="Iban"
            name="iban"
            value={this.state.iban || ''}
            onChange={this.handleChange("iban")}
            validators={['required']}
            errorMessages={['this field is required']}/>
        </Grid>

        <Grid item xs={3}>
          <TextValidator
            required
            fullWidth
            label="Bic"
            name="bic"
            value={this.state.bic || ''}
            onChange={this.handleChange("bic")}
            validators={['required']}
            errorMessages={['this field is required']}/>
        </Grid>

        <Grid item xs={2}>
          <SelectValidator
            required
            fullWidth
            label={"Country"}
            value={this.state.country || ''}
            onChange={this.handleChange("country")}
            input={<Input name="age" id="country-label-placeholder"/>}
            name="country"
          >
            <MenuItem value="NL">NL</MenuItem>
            <MenuItem value="BE">BE</MenuItem>
            <MenuItem value="FR">FR</MenuItem>
          </SelectValidator>
        </Grid>
      </Grid>

    )
  }

}

export default PaymentBankAccountForm;
