import React from "react";

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableFooter from '@material-ui/core/TableFooter';
import TablePagination from '@material-ui/core/TablePagination';

class DonationTable extends React.Component {

  handleChangePage(ev) {

  }

  render() {

    const {data} = this.props;

    return (
      <Table style={{minWidth: 1000}}>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Amount</TableCell>
            <TableCell>Frequency</TableCell>
            <TableCell>Collection Month</TableCell>
            <TableCell>Start Date</TableCell>
            <TableCell>End Date</TableCell>
            <TableCell>Type</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>{data.map(it => {
          return (
            <TableRow
              key={it.id}
              hover
              onClick={event => this.handleRowClick(event, it)}
            >
              <TableCell scope="row">{this.memberToName(it.member)}</TableCell>
              <TableCell>&euro; {it.mandate.amount},-</TableCell>
              <TableCell>{it.mandate.frequency}</TableCell>
              <TableCell>{it.mandate.collectionMonth}</TableCell>
              <TableCell>{this.getFormattedDate(it.mandate.startDate)}</TableCell>
              <TableCell>{this.getFormattedDate(it.mandate.endDate)}</TableCell>
              <TableCell>{it.mandate.type}</TableCell>
            </TableRow>
          )
        })}
        </TableBody>
        <TableFooter>
          <TableRow>
            <TablePagination
              count={this.props.count}
              rowsPerPage={this.props.size}
              page={this.props.page}
              rowsPerPageOptions={[]}
              onChangePage={this.props.onChangePage}
            />
          </TableRow>
        </TableFooter>
      </Table>
    )
  }

  memberToName(member) {
    if (!member)
      return 'Anonymous'
    if (member.infix) {
      return `${member.firstName} ${member.infix} ${member.surName}`
    }
    return `${member.firstName} ${member.surName}`
  }

  handleRowClick(event, donation) {
    if (this.props.handleRowClick)
      return this.props.handleRowClick(donation)
  }

  getFormattedDate(dateStr) {
    if (!dateStr) return;
    const date = new Date(Date.parse(dateStr))
    return date.toLocaleDateString();
  }

  desc(a, b, orderBy) {
    if (b[orderBy] < a[orderBy]) {
      return -1;
    }
    if (b[orderBy] > a[orderBy]) {
      return 1;
    }
    return 0;
  }

  getSorting(order, orderBy) {
    return order === 'desc' ? (a, b) => desc(a, b, orderBy) : (a, b) => -desc(a, b, orderBy);
  }

}

export default DonationTable;