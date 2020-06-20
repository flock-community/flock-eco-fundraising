import React from "react";
import {withStyles} from '@material-ui/core/styles';

import Grid from '@material-ui/core/Grid';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';


import MailchimpCampaignTable from '@flock-community/flock-eco-feature-mailchimp/src/main/react/campaign/MailchimpCampaignTable'
import MailchimpTemplateTable from '@flock-community/flock-eco-feature-mailchimp/src/main/react/template/MailchimpTemplateTable'
import MailchimpMemberTable from '@flock-community/flock-eco-feature-mailchimp/src/main/react/member/MailchimpMemberTable'

import AuthorityUtil from '../utils/AuthorityUtil'

const styles = theme => ({
  card: {
    marginBottom: 10
  },
});

class MailchimpFeature extends React.Component {

  handleMemberRowClick(obj) {
    const url = `https://admin.mailchimp.com/lists/members/view?id=${obj.webId}`
    window.open(url, '_blank');
  }


  handleCampaignRowClick(obj) {
    const url = `https://admin.mailchimp.com/campaigns/edit?id=${obj.webId}`
    window.open(url, '_blank');
  }

  handleTemplateRowClick(obj) {
    const url = `https://admin.mailchimp.com/templates/edit?id=${obj.id}`
    window.open(url, '_blank');
  }

  handleSyncClick(ev) {
    fetch(`/api/mailchimp/sync`)
  }

  render() {

    const {classes} = this.props;

    return (
      <React.Fragment>
        <Grid container spacing={1}>

          <Grid item xs={8}>

            <Card className={classes.card}>
              <CardContent>
                <Button variant="contained" color="primary" onClick={this.handleSyncClick}>Sync</Button>
              </CardContent>
            </Card>

            <AuthorityUtil has="MailchimpMemberAuthority.READ">
              <Card className={classes.card}>
                <CardContent>
                  <Typography variant="h6">
                    Members
                  </Typography>
                </CardContent>
                <MailchimpMemberTable
                  onRowClick={this.handleMemberRowClick}/>
              </Card>
            </AuthorityUtil>


          </Grid>

          <Grid item xs={4}>

            <AuthorityUtil has="MailchimpCampaignAuthority.READ">

              <Card className={classes.card}>
                <CardContent>
                  <Typography variant="h6">
                    Campaigns
                  </Typography>
                </CardContent>
                <MailchimpCampaignTable
                  onRowClick={this.handleCampaignRowClick}/>
              </Card>

            </AuthorityUtil>

            <AuthorityUtil has="MailchimpTemplateAuthority.READ">

              <Card className={classes.card}>
                <CardContent>
                  <Typography variant="h6">
                    Templates
                  </Typography>
                </CardContent>
                <MailchimpTemplateTable
                  onRowClick={this.handleTemplateRowClick}/>
              </Card>
            </AuthorityUtil>
          </Grid>

        </Grid>
      </React.Fragment>
    )
  }
};

export default withStyles(styles)(MailchimpFeature);
