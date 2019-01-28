import React from "react";
import {withStyles} from '@material-ui/core/styles';

import MemberDeletedFormSepa from "./SystemConfigurationFormSepa";

const styles = theme => ({});

class SystemConfigurationFeature extends React.Component {

  state = {
    data: []
  }

  componentDidMount() {
    this.loadData()
  }

  loadData() {
    fetch(`/api/system-configuration/properties`)
      .then(res => {
        return res.json()
      })
      .then(json => {
        this.setState({data: json});
      })
  }

  render() {

    const {classes} = this.props;
    const {data} = this.state;

    return (
      <React.Fragment>
        <MemberDeletedFormSepa data={data}/>
      </React.Fragment>
    )
  }

}

export default withStyles(styles)(SystemConfigurationFeature);