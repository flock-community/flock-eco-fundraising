import React from "react";

import Chip from '@material-ui/core/Chip';

import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableFooter from '@material-ui/core/TableFooter';
import TablePagination from '@material-ui/core/TablePagination';

class DonationTable extends React.Component {

  state = {
    data: [],
    count: 0,
    page: 0,
    size: 10,
    anchorEl: null
  }

  handleChangePage = (ev, page) => {
    this.setState({page: page}, () => {
      this.loadData()
    })
  }

  handleRowStatusClick = (item) => (ev) => {
    this.setState({item, anchorEl: ev.currentTarget})
  }

  handleItemStatusClick = (status) => (ev) => {
    this.saveStatus(this.state.item, status)
    this.setState({anchorEl: null})
  }

  componentDidMount() {
    this.loadData()
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (prevProps.year !== this.props.year) return this.loadData()
    if (prevProps.month !== this.props.month) return this.loadData()
    if (prevProps.reload !== this.props.reload) return this.loadData()
  }

  loadData() {

    const {year, month} = this.props;

    fetch(`/api/transactions/${year}/${month}?page=${this.state.page}&size=${this.state.size}`)
      .then(res => {
        this.setState({
          count: parseInt(res.headers.get('x-total')),
          page: parseInt(res.headers.get('x-page'))
        })
        return res.json()
      })
      .then(json => {
        this.setState({data: json});
      })
      .catch(e => {
        this.setState({message: "Cannot load members"})
      })
  }

  saveStatus = (it, status) => {
    const transaction = it.transaction
    const opts = {
      method: "POST",
    }
    fetch(`/api/transactions/${transaction.id}/status/${status}`, opts)
      .then(res => {
        if (res.status === 200) {
          this.loadData();
        }
      })
  }

  render() {

    const {data} = this.state;

    return (
      <React.Fragment>
        <Table style={{minWidth: 1000}}>
          <TableHead>
            <TableRow>
              <TableCell>Mandate#</TableCell>
              <TableCell>Transaction#</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Iban</TableCell>
              <TableCell>Frequency</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>{data.map(it => {
            return (
              <TableRow
                key={it.transaction.id}
                hover
                onClick={event => this.handleRowClick(event, it)}
              >
                <TableCell style={{width: 30}}>{this.showId(it.mandate.code)}</TableCell>
                <TableCell style={{width: 30}}>{this.showId(it.transaction.reference)}</TableCell>
                <TableCell>{it.mandate.bankAccount.name}</TableCell>
                <TableCell>{it.mandate.bankAccount.iban}</TableCell>
                <TableCell>{it.mandate.frequency}</TableCell>
                <TableCell>&euro; {it.mandate.amount},-</TableCell>
                <TableCell>
                  <Chip
                    label={it.transaction.status}
                    style={{backgroundColor: this.color(it)}}
                    onClick={this.handleRowStatusClick(it)}/>
                </TableCell>
              </TableRow>
            )
          })}
          </TableBody>
          <TableFooter>
            <TableRow>
              <TablePagination
                count={this.state.count}
                rowsPerPage={this.state.size}
                page={this.state.page}
                rowsPerPageOptions={[]}
                onChangePage={this.handleChangePage}
              />
            </TableRow>
          </TableFooter>
        </Table>
        <Menu
          id="status-menu"
          anchorEl={this.state.anchorEl}
          open={Boolean(this.state.anchorEl)}
          onClose={this.handleItemStatusClick(null)}
        >
          <MenuItem onClick={this.handleItemStatusClick("PENDING")}>Pending</MenuItem>
          <MenuItem onClick={this.handleItemStatusClick("CANCELED")}>Canceled</MenuItem>
          <MenuItem onClick={this.handleItemStatusClick("SUCCESS")}>Success</MenuItem>
          <MenuItem onClick={this.handleItemStatusClick("ERROR")}>Error</MenuItem>
        </Menu>
      </React.Fragment>
    )
  }

  color(it) {
    const status = it.transaction.status
    if (status === 'PENDING') return 'orange'
    if (status === 'CANCELED') return 'gray'
    if (status === 'SUCCESS') return 'green'
    if (status === 'ERROR') return 'red'
    else return null
  }

  handleRowClick(event, donation) {
    if (this.props.handleRowClick)
      return this.props.handleRowClick(donation)
  }

  showId(id) {
    if (id.length > 8) return id.substring(0, 8) + '...'
    else return id
  }

}

export default DonationTable;