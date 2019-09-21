import React from 'react'
import {Field} from 'formik'
import Grid from "@material-ui/core/Grid";
import {RadioGroup} from "formik-material-ui";
import {useIntl} from "react-intl";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Radio from "@material-ui/core/Radio";

export function DonationAmountForm({amounts, isSubmitting}) {

  const {formatNumber} = useIntl();


  return (
    <Grid container spacing={1}>
      <Grid item xs={12}>
        <Field name="amount" component={RadioGroup}>
          {(amounts || [5, 10, 50])
            .map(amount => String(amount))
            .map((amount, i) => (<FormControlLabel
            key={`amount-${i}`}
            value={amount}
            control={<Radio disabled={isSubmitting}/>}
            label={formatNumber(amount, {style: 'currency', currency: 'EUR'})}
            disabled={isSubmitting}
          />))}

        </Field>
      </Grid>
    </Grid>)
}

