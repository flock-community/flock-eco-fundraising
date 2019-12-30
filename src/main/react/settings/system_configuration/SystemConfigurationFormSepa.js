import React from "react";
import {withStyles} from '@material-ui/core/styles';

import Grid from '@material-ui/core/Grid';

import {TextValidator, ValidatorForm} from 'react-material-ui-form-validator'

const styles = theme => ({
  card: {
    marginBottom: 10
  },
});

class SystemConfigurationFeature extends React.Component {

  state = {
    "flock.fundraising.name": null
  }

  handleSubmit(name) {
    this.setState({[name]: event.target.value})
  }

  handleChange = (name) => event => {
    console.log(name)
    this.setState({[name]: event.target.value})
  }

  render() {

    const {classes} = this.props;

    return (
      <React.Fragment>

        <ValidatorForm
          id="system-configuration-sepa-form"
          onSubmit={this.handleSubmit}
          onError={errors => console.log(errors)}
        >
          <Grid container spacing={1}>
            <Grid item xs={5}>
              <TextValidator
                fullWidth
                required
                name="name"
                label="Application name"
                className={classes.input}
                value={this.state['flock.fundraising.name'] || ''}
                onChange={this.handleChange('flock.fundraising.name')}
                validators={['required']}
                errorMessages={['this field is required']}
              />
            </Grid>
          </Grid>
        </ValidatorForm>

      </React.Fragment>
    )
  }

}

export default withStyles(styles)(SystemConfigurationFeature);
