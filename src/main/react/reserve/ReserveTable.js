import React from "react";

import {Link} from 'react-router-dom'

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';

import Paper from '@material-ui/core/Paper';

class ReserveTable extends React.Component {

  state = {
    list:[]
  }

  componentDidMount(){
    this.handleLoad()
  }

  componentDidUpdate(prevProps) {
    if (this.props.reload !== prevProps.reload && !prevProps.reload) {
      this.handleLoad()
    }
  }

  handleLoad = () => {
    fetch('/api/reservations')
      .then(res => res.json())
      .then(json => {
        this.setState({list: json});
      });
  }

  handleClick(item) {
    if (this.props.onRowClick)
      return this.props.onRowClick(item)
  }

  render() {

    return (
      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Address</TableCell>
              <TableCell>Action</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>{this.state.list.map(it => {
            return (
              <TableRow
                key={it.id}
                hover
                onClick={event => this.handleClick(it)}
              >
                <TableCell>{it.name}</TableCell>
                <TableCell>{it.address}</TableCell>
                <TableCell>
                  <Link to={`/reservations/${it.id}`}>Statistics</Link>
                </TableCell>
              </TableRow>
            )
          })}
          </TableBody>
        </Table>
      </Paper>
    )
  }

}

export default ReserveTable;