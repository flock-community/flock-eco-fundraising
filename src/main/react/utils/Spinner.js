import Grid from '@material-ui/core/Grid'
import CircularProgress from '@material-ui/core/CircularProgress'
import React from 'react'
import {withStyles} from '@material-ui/core'

const spinner = ({
        flexDirection:"column",
        alignItems:"center",
        justifyContent:"center"
})

export const Spinner = () => {
    return (
        <Grid container style={spinner}>
            <Grid item xs>
                <CircularProgress />
            </Grid>
        </Grid>
    )
}