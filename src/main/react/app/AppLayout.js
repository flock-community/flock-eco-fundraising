import React from 'react';


import PropTypes from 'prop-types';
import classNames from 'classnames';
import {MuiThemeProvider, withStyles} from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';

import Button from '@material-ui/core/Button';

import MenuIcon from '@material-ui/icons/Menu';
import InfoIcon from '@material-ui/icons/Info';

import AppDrawer from './AppDrawer'

const drawerWidth = 240;

const styles = theme => ({
  root: {
    flexGrow: 1,
    zIndex: 1,
    position: 'relative',
    display: 'flex',
  },
  appBar: {
    postion: 'fixed',
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
  },
  appBarShift: {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  menuButton: {
    marginLeft: 12,
    marginRight: 36,
  },
  hide: {
    display: 'none',
  },
  drawerPaper: {
    position: 'relative',
    whiteSpace: 'nowrap',
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerPaperClose: {
    overflowX: 'hidden',
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    width: theme.spacing.unit * 7,
    [theme.breakpoints.up('sm')]: {
      width: theme.spacing.unit * 9,
    },
  },
  toolbar: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
  },
  main: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.default,
    height: '100vh',
    overflow: 'scroll'
  },
  content: {
    backgroundColor: theme.palette.background.default,
    padding: '100px 24px 24px 24px',
    overflow: 'hidden'
  },
  grow: {
    flexGrow: 1,
  },

});

class AppLayout extends React.Component {

  constructor(props) {

    super(props);

    this.state = {
      open: false,
    };

    this.handleDrawerOpen = () => {
      this.setState({open: true});
    };

    this.handleDrawerClose = () => {
      this.setState({open: false});
    };

    this.handleInfoOpen = () => {
      const website = 'https://willemveelenturf.wixsite.com/doneasy';
      window.open(website + location.hash.substring(1), "_blank")
    }

  }


  render() {
    const {classes, theme} = this.props;

    return (
      <MuiThemeProvider theme={theme}>
        <div className={classes.root}>
          <AppBar
            position="fixed"
            className={classNames(classes.appBar, this.state.open && classes.appBarShift)}
          >
            <Toolbar disableGutters={!this.state.open}>

              <IconButton
                color="inherit"
                aria-label="Open drawer"
                onClick={this.handleDrawerOpen}
                className={classNames(classes.menuButton, this.state.open && classes.hide)}
              >
                <MenuIcon/>
              </IconButton>

              <Typography variant="h6" color="inherit" noWrap className={classes.grow}>
                {this.props.title || "App name"}
              </Typography>


              <Button
                color="inherit"
                aria-label="Open drawer"
                onClick={this.handleInfoOpen}
                className={classNames(classes.menuButton, this.state.open && classes.hide)}
              >
                <InfoIcon/>
              </Button>

            </Toolbar>
          </AppBar>

          <AppDrawer
            authorities={this.state.authorities}
            handleDrawerClose={this.handleDrawerClose}
            theme={theme}
            open={this.state.open}/>

          <main className={classes.main}>
            <div className={classes.content}>
              {this.props.children}
            </div>
          </main>
        </div>
      </MuiThemeProvider>
    );
  }

}

AppLayout.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles)(AppLayout);