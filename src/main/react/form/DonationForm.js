import React, {useEffect, useState} from 'react'

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
import {DonationLocalization} from "./DonationLocalization";
import {Spinner} from "../utils/Spinner";


const translationFiles = {
    'en-GB': enGB,
    'nl-NL': nlNL
};

function DonationForm({onSubmit}) {


    const locale = 'nl-NL'

    const [state, setState] = useState({locale: 'nl-NL'})

    useEffect( () => {
        const intl = createIntl(
            {
                locale: state.locale,
                messages: translationFiles[state.locale]
            },
        );

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

        setState({...state, intl, schema });
    },[state.locale])

    const handleSwitchLocal = (value) => {
        console.log(value)
        setState({...state, locale: value.name})
    }

    const handleSubmit = (data) => {
        console.log(data)
    }

    const renderForm = (props) => {
        return (
            <Form>
            <DonationLocalization value={state} onChange={handleSwitchLocal}/>
            <DonationAmountForm {...props}/>
            <Typography>Hoe wilt u betalen?</Typography>
            <DonationPaymentIdealForm {...props} />
            <DonationPaymentCreditcardForm {...props} />
            <Typography>Wat zijn uw gegevens?</Typography>
            <DonationMemberForm {...props}/>
            <Button type="submit" variant="contained" color="primary">Bevestig</Button>
        </Form>)
    }

    if (!state.intl && !state.schema) {
        return <Spinner/>
    }

    return (
        <RawIntlProvider value={state.intl}>
            <Formik
                initialValues={state.schema.cast()}
                validationSchema={state.schema}
                onSubmit={handleSubmit}
                render={renderForm}/>
        </RawIntlProvider>)

}

export default DonationForm
