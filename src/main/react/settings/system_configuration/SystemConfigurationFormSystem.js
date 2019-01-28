import React from "react";
import {withStyles} from '@material-ui/core/styles';

import Grid from '@material-ui/core/Grid';

import Typography from '@material-ui/core/Typography';

import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';

const styles = theme => ({
  card: {
    marginBottom: 10
  },
});

class SystemConfigurationFeature extends React.Component {

  state = {
    data:[]
  }

  componentDidMount() {
    this.loadData()
  }

  loadData() {
    fetch(`/api/system-configuration`)
      .then(res => {
        return res.json()
      })
      .then(json => {
        this.setState({data: json});
      })
  }

  render() {

    const {classes} = this.props;

    if (!this.state.data)
      return null

    return (
      <React.Fragment>
        <Card>
          {this.renderTable()}
        </Card>

      </React.Fragment>
    )
  }

  renderTable() {
    return(<Table>
      <TableHead>
        <TableRow>
          <TableCell>Name</TableCell>
          <TableCell>Email</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>{this.state.data.map(it => (
        <TableRow
          key={it.id}
          hover
          onClick={event => this.handleClick(it)}
        >
          <TableCell>{this.memberToName(it)}</TableCell>
          <TableCell>{it.email}</TableCell>
        </TableRow>
      ))}
      </TableBody>
    </Table>)
  }

  memberToName(it) {
    if (it.infix) {
      return `${it.firstName} ${it.infix} ${it.surName}`
    }
    return `${it.firstName} ${it.surName}`
  }
}

export default withStyles(styles)(SystemConfigurationFeature);