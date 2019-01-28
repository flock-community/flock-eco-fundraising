import React from "react";

import Button from '@material-ui/core/Button';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableFooter from '@material-ui/core/TableFooter';

import TextField from '@material-ui/core/TextField';

import Paper from '@material-ui/core/Paper';

import {withRouter} from 'react-router-dom'
import {withStyles} from "@material-ui/core/styles/index";

const styles = theme => ({
  tableCell: {
    width: 100
  },
  numberInput: {
    width: 100
  },
});


class ReserveStatistics extends React.Component {

  state = {
    statistics: {}
  }

  componentDidMount() {
    this.handleLoad()
  }

  handleLoad = () => {
    fetch(`/api/reservations/${this.props.reserve}`)
      .then(res => res.json())
      .then(json => {
        this.setState({reserve: json});
      });

    fetch(`/api/reservations/${this.props.reserve}/statistics`)
      .then(res => res.json())
      .then(json => {
        this.setState({statistics: json.reduce((acc, cur) => {
          acc[cur.year] = cur
          return acc
        }, {})});
      });
  }

  findValue = (name) => (year) => {
    this.state.statistics[year] = this.state.statistics[year] || {}
    return this.state.statistics[year][name] || ''
  }

  handleChange = (name) => (year) => (ev) => {
    const value = ev.target.value
    const data = this.state[year] || {}
    data[name] = value


    this.setState(Object.assign(this.state.statistics, {[year]: data}))
  }

  handleSubmit = () => {
    const body = Object.keys(this.state.statistics).map((year) => {
      const statistic = this.state.statistics[year]
      return Object.assign(statistic, {year})
    })
    const opts = {
      method: "POST",
      headers: {
        "Content-Type": "application/json; charset=utf-8",
      },
      body: JSON.stringify(body)
    };
    fetch(`/api/reservations/${this.props.reserve}/statistics`, opts)
      .then(res => {
        if(res.ok){
            this.props.history.push('/reservations')
        }
      })
  }

  handleCancel = () =>{
    this.props.history.push('/reservations')
  }

  render() {

    const {classes} = this.props;

    const date = new Date()
    const startYear = 2000
    const endYear = date.getFullYear()
    const range = Array
      .from(Array(endYear - startYear + 1).keys())
      .map(x => x + startYear)

    return (
      <Paper>

        <form onSubmit={this.handleSubmit}>
          <Table
            padding="dense">
            <TableHead>
              <TableRow>
                <TableCell className={classes.tableCell}>Year</TableCell>
                <TableCell className={classes.tableCell}>Total count</TableCell>
                <TableCell className={classes.tableCell}>Accidents count</TableCell>
                <TableCell className={classes.tableCell}>Killed count</TableCell>
                <TableCell/>
              </TableRow>
            </TableHead>
            <TableBody>{range.map(it => {

              return (
                <TableRow
                  key={it}
                >
                  <TableCell>{it}</TableCell>
                  <TableCell className={classes.tableCell}>
                    <TextField
                      fullWidth
                      type="number"
                      className={classes.numberInput}
                      value={this.findValue('countTotal')(it)}
                      onChange={this.handleChange('countTotal')(it)}/>
                  </TableCell>
                  <TableCell className={classes.tableCell}>
                    <TextField
                      fullWidth
                      type="number"
                      className={classes.numberInput}
                      value={this.findValue('countAccidents')(it)}
                      onChange={this.handleChange('countAccidents')(it)}/>
                  </TableCell>
                  <TableCell className={classes.tableCell}>
                    <TextField
                      fullWidth
                      type="number"
                      className={classes.numberInput}
                      value={this.findValue('countKilled')(it)}
                      onChange={this.handleChange('countKilled')(it)}/>
                  </TableCell>
                </TableRow>
              )
            })}
            </TableBody>
            <TableFooter>
              <TableRow >
              <TableCell colSpan={6} numeric>
                <Button type="submit" onClick={this.handleCancel} color="primary">
                  Cancel
                </Button>
                <Button type="submit" color="primary">
                  Save
                </Button>
              </TableCell>
              </TableRow>
            </TableFooter>
          </Table>
        </form>
      </Paper>
    )
  }

}

export default withRouter(withStyles(styles)(ReserveStatistics));