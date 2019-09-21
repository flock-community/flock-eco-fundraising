import React from 'react'

import {RadioGroup, TextField} from 'formik-material-ui'
import {Field} from 'formik'
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Radio from "@material-ui/core/Radio";
import Grid from "@material-ui/core/Grid";
import {useIntl} from "react-intl";

export function DonationMemberForm ({isSubmitting, ...props}) {

  const {formatMessage} = useIntl();

  return(
    <Grid container spacing={1}>
      <Grid item xs={12}>
        <Field name="member.gender" row component={RadioGroup}>
          <FormControlLabel
            value="MALE"
            control={<Radio disabled={isSubmitting}/>}
            label={formatMessage({id: 'form.donation.member.gender.male.label'})}
            disabled={isSubmitting}
          />
          <FormControlLabel
            value="FEMALE"
            control={<Radio disabled={isSubmitting}/>}
            label={formatMessage({id: 'form.donation.member.gender.female.label'})}
            labelPlacement="end"
            disabled={isSubmitting}
          />
          <FormControlLabel
            value="OTHER"
            control={<Radio disabled={isSubmitting}/>}
            label={formatMessage({id: 'form.donation.member.gender.other.label'})}
            labelPlacement="end"
            disabled={isSubmitting}
          />
        </Field>
      </Grid>
      <Grid item xs={4}>
        <Field
          name="member.firstName"
          label={formatMessage({id: 'form.donation.member.firstName.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>
      <Grid item xs={3}>
        <Field
          name="member.middleName"
          label={formatMessage({id: 'form.donation.member.middleName.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>
      <Grid item xs={5}>
        <Field
          name="member.lastName"
          label={formatMessage({id: 'form.donation.member.lastName.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>

      <Grid item xs={8}>
        <Field
          name="member.street"
          label={formatMessage({id: 'form.donation.member.street.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>
      <Grid item xs={2}>
        <Field
          name="member.houseNumber"
          label={formatMessage({id: 'form.donation.member.houseNumber.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>
      <Grid item xs={2}>
        <Field
          name="member.houseNumberExtension"
          label={formatMessage({id: 'form.donation.member.houseNumberExtension.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>

      <Grid item xs={4}>
        <Field
          name="member.postalCode"
          label={formatMessage({id: 'form.donation.member.postalCode.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>
      <Grid item xs={8}>
        <Field
          name="member.city"
          label={formatMessage({id: 'form.donation.member.city.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>
      <Grid item xs={8}>
        <Field
          name="member.email"
          label={formatMessage({id: 'form.donation.member.email.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>
      <Grid item xs={8}>
        <Field
          name="member.phoneNumber"
          label={formatMessage({id: 'form.donation.member.phoneNumber.label'})}
          fullWidth
          variant="outlined"
          component={TextField}/>
      </Grid>
    </Grid>)
}

