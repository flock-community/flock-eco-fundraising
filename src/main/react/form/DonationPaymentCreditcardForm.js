import React from 'react'
import {Field} from 'formik'
import Grid from "@material-ui/core/Grid";
import MenuItem from "@material-ui/core/MenuItem";
import FormControl from "@material-ui/core/FormControl";
import InputLabel from "@material-ui/core/InputLabel";
import OutlinedInput from "@material-ui/core/OutlinedInput";
import {Select} from "formik-material-ui";
import FormHelperText from "@material-ui/core/FormHelperText";
import {useIntl} from "react-intl";


const issuers = [
  {name: 'MasterCard', code: 'mastercard'},
  {name: 'American Express', code: 'Amex'},
]

export function DonationPaymentCreditcardForm({errors, touched}) {

  const {formatMessage} = useIntl();

  const touch = touched && touched.payment && touched.payment.creditCard && touched.payment.creditCard.issuer
  const error = errors && errors.payment && errors.payment.creditCard && errors.payment.creditCard.issuer

  return (
    <Grid container spacing={1}>

      <Grid item xs={12}>
        <FormControl fullWidth error={(touch && error) || false}>
          <InputLabel htmlFor="payment-creditCard-bank">Credit card</InputLabel>
          <Field
            name="payment.creditCard.issuer"
            label="Bank"
            fullWidth
            input={<OutlinedInput id="payment-creditCard-issuer"/>}
            component={Select}>
            <MenuItem value={''}><em>{formatMessage({id: 'form.common.choose'})}</em></MenuItem>
            {issuers
              .map((issuer, i) => (
                <MenuItem key={`payment-creditCard-bank-item-${i}`} value={issuer.code}>{issuer.name}</MenuItem>))}
          </Field>
          {touch && <FormHelperText>{error}</FormHelperText>}
        </FormControl>

      </Grid>
    </Grid>)
}

