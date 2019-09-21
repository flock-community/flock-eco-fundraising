import React from 'react'

import {Form, Formik} from 'formik'
import {DonationMemberForm} from "./DonationMemberForm";
import {createIntl, RawIntlProvider} from 'react-intl';

import enGB from '../messages/en-GB.js'
import nlNL from '../messages/nl-NL.js'
import * as Yup from "yup";
import {Typography} from "@material-ui/core";
import {DonationPaymentIdealForm} from "./DonationPaymentIdealForm";
import Button from "@material-ui/core/Button";
import {DonationPaymentCreditcardForm} from "./DonationPaymentCreditcardForm";
import {DonationAmountForm} from "./DonationAmountForm";


const locale = 'nl-NL'

const messages = {
  'en-GB': enGB,
  'nl-NL': nlNL
};

const intl = createIntl({
  locale,
  messages: messages[locale]
});

function DonationForm({init, onSubmit}) {

  const schema = Yup.object({
    amount: Yup.string()
      .default(''),
    payment: Yup.object({
      ideal: Yup.object({
        bank: Yup.string()
          .default('')
          .required(intl.formatMessage({id: 'form.validation.required'})),
      }),
      creditCard: Yup.object({
        issuer: Yup.string()
          .default('')
          .required(intl.formatMessage({id: 'form.validation.required'})),
      })
    }),
    member: Yup.object({
      gender: Yup.string()
        .default('')
        .required(intl.formatMessage({id: 'form.validation.required'})),
      firstName: Yup.string()
        .default('')
        .required(intl.formatMessage({id: 'form.validation.required'})),
      middleName: Yup.string()
        .default(''),
      lastName: Yup.string('Enter your password')
        .default('')
        .required(intl.formatMessage({id: 'form.validation.required'})),
      street: Yup.string()
        .default('')
        .required(intl.formatMessage({id: 'form.validation.required'})),
      houseNumber: Yup.string()
        .default('')
        .required(intl.formatMessage({id: 'form.validation.required'})),
      houseNumberExtention: Yup.string()
        .default(''),
      postalCode: Yup.string()
        .default('')
        .required(intl.formatMessage({id: 'form.validation.required'})),
      city: Yup.string()
        .default('')
        .required(intl.formatMessage({id: 'form.validation.required'})),
      email: Yup.string('Enter your email')
        .default('')
        .email(intl.formatMessage({id: 'form.validation.email'}))
        .required(intl.formatMessage({id: 'form.validation.required'}, {field: 'Email'})),
      phoneNumber: Yup.string()
        .default(''),
    })
  })

  const handleSubmit = (data) => {
    console.log(data)
  }

  const renderForm = (props) => {
    return (<Form>
      <DonationAmountForm {...props}/>
      <Typography>Hoe wilt u betalen?</Typography>
      <DonationPaymentIdealForm {...props} />
      <DonationPaymentCreditcardForm {...props} />
      <Typography>Wat zijn uw gegevens?</Typography>
      <DonationMemberForm {...props}/>
      <Button type="submit" variant="contained" color="primary" >Bevestig</Button>
    </Form>)
  }

  return (
    <RawIntlProvider value={intl}>
      <Formik
        initialValues={schema.cast()}
        validationSchema={schema}
        onSubmit={handleSubmit}
        render={renderForm}/>
    </RawIntlProvider>)

}

export default DonationForm
