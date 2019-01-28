import React from "react";
import {withStyles} from '@material-ui/core/styles';

import Button from '@material-ui/core/Button';
import AddIcon from '@material-ui/icons/Add';

import ReserveTable from './ReserveTable.js'
import ReserveDialog from './ReserveDialog.js'

const styles = theme => ({
  button: {
    position: 'fixed',
    right: 20,
    bottom: 20,
    margin: theme.spacing.unit,
  }
});

class ReserveFeature extends React.Component {

  state = {
    item: null,
    list: []
  }

  handleRowClick = (item) => {
    this.setState({item})
  }

  handleNewClick = (ev) => {
    this.setState({item: {}})
  }

  handleComplete = (ev) => {

    this.setState({item: null}, () => {
      this.handleLoad()
    })
  }

  handleLoad = () => {

  }

  render() {

    const {classes} = this.props;

    return (

      <div>

        <ReserveTable
          reload={this.state.item === null}
          onRowClick={this.handleRowClick}/>

        <ReserveDialog
          value={this.state.item}
          onComplete={this.handleComplete}/>

        <Button
          variant="fab"
          color="primary"
          aria-label="Add"
          className={classes.button}
          onClick={this.handleNewClick}
        >
          <AddIcon/>
        </Button>

      </div>
    )
  }
};

export default withStyles(styles)(ReserveFeature);