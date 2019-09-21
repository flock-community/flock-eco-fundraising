import React from 'react'
import {Field, Form} from 'formik'
import * as Yup from 'yup'
import Grid from "@material-ui/core/Grid";
import MenuItem from "@material-ui/core/MenuItem";
import FormControl from "@material-ui/core/FormControl";
import InputLabel from "@material-ui/core/InputLabel";
import OutlinedInput from "@material-ui/core/OutlinedInput";
import {Select} from "formik-material-ui";
import FormHelperText from "@material-ui/core/FormHelperText";
import {formatMessage, useIntl} from "react-intl";


const banks = [
  {name: 'ABN AMRO', code: 'ABNANL2A'},
  {name: 'ASN Bank', code: 'ASNBNL21'},
  {name: 'ING Bank', code: 'INGBNL2A'},
  {name: 'Rabobank', code: 'ABNANL2A'},
  {name: 'SNS Regio Bank', code: 'SNSBNL2A'},
  {name: 'Van Lanschot', code: 'FVLBNL22'},
  {name: 'Triodos Bank', code: 'TRIONL2U'},
  {name: 'Knab', code: 'KNABNL2H'},
  {name: 'Bunq', code: 'BUNQNL2A'},
  {name: 'MOYONL21', code: 'KNABNL2H'},
]

export const idealPaymentValidation = Yup.object({
  bank: Yup.string('Enter your email')
})

export function DonationPaymentIdealForm ({errors, touched}) {

  const {formatMessage} = useIntl();

  const touch = touched && touched.payment && touched.payment.ideal && touched.payment.ideal.bank
  const error = errors && errors.payment && errors.payment.ideal && errors.payment.ideal.bank

  return (
    <Grid container spacing={1}>

      <Grid item xs={12}>
        <FormControl fullWidth error={(touch && error) || false}>
          <InputLabel htmlFor="payment-ideal-bank">Bank</InputLabel>
          <Field
            name="payment.ideal.bank"
            label="Bank"
            fullWidth
            input={<OutlinedInput id="payment-ideal-bank"/>}
            component={Select}>
            <MenuItem value={''}><em>{formatMessage({id: 'form.common.choose'})}</em></MenuItem>
            {banks
              .map((bank, i) => (<MenuItem key={`payment-ideal-bank-item-${i}`} value={bank.code}>{bank.name}</MenuItem>))}
          </Field>
          {touch && <FormHelperText>{error}</FormHelperText>}
        </FormControl>

      </Grid>
    </Grid>)
}

