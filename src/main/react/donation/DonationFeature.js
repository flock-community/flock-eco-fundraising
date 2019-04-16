import React from "react";
import {withStyles} from '@material-ui/core/styles';

import Grid from '@material-ui/core/Grid';

import Fab from '@material-ui/core/Fab';

import Card from '@material-ui/core/Card';
import Paper from '@material-ui/core/Paper';
import CardContent from '@material-ui/core/CardContent';
import TextField from '@material-ui/core/TextField';

import AddIcon from '@material-ui/icons/Add';

import DonationTable from "./DonationTable";
import DonationDialog from "./DonationDialog";

const styles = theme => ({
  tablePaper: {
    marginBottom: 50,
    width: '100%',
    marginTop: theme.spacing.unit * 3,
    overflowX: 'auto',
  },
  table: {
    width: 1000,
  },
  button: {
    position: 'fixed',
    right: 20,
    bottom: 20,
    margin: theme.spacing.unit,
  }
});

class DonationFeature extends React.Component {

  state = {
    item: null,
    count: 0,
    page: 0,
    size: 10,
    search: "",
    list: []
  }

  componentDidMount() {
    this.loadData()
  }

  loadData() {
    fetch(`/api/donations?s=${this.state.search}&page=${this.state.page}&size=${this.state.size}`)
      .then(res => {
        this.setState({
          count: parseInt(res.headers.get('x-total'))
        })
        return res.json()
      })
      .then(json => {
        this.setState({list: json});
      })
      .catch(e => {
        this.setState({message: "Cannot load members"})
      })
  }

  handleCompleteDonation = () => {
    this.setState({
      item: null,
    }, () => {
      this.loadData()
    })
  }

  handleChangePage = (ev, page) => {
    this.setState({page}, () => {
      this.loadData()
    })
  }

  handleChangeSearch = (ev) => {
    this.setState({
      page: 0,
      search: ev.target.value
    }, () => {
      this.loadData()
    });

  };

  handleRowClick = (item) => {
    this.setState({item: item.id})
  }

  handleNewClick = () => {
    this.setState({item: 0})
  }

  render() {

    const {classes} = this.props;

    return (
      <React.Fragment>
        <Grid container spacing={16}>
          <Grid item xs={12}>
            <Card>
              <CardContent style={{textAlign: 'right'}}>
                <Grid container spacing={0}>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Search"
                      value={this.state.search}
                      className={classes.textField}
                      onChange={this.handleChangeSearch}/>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12}>
            <Paper className={classes.tablePaper}>
              <DonationTable
                className={classes.table}
                data={this.state.list}
                count={this.state.count}
                page={this.state.page}
                size={this.state.size}
                handleRowClick={this.handleRowClick}
                onChangePage={this.handleChangePage}
              />

              <DonationDialog
                id={this.state.item}
                onComplete={this.handleCompleteDonation}/>

              <Fab
                color="primary"
                aria-label="Add"
                className={classes.button}
                onClick={this.handleNewClick}
              >
                <AddIcon/>
              </Fab>
            </Paper>
          </Grid>
        </Grid>
      </React.Fragment>
    )
  }
};

export default withStyles(styles)(DonationFeature);