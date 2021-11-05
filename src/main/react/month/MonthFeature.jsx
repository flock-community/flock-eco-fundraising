import React from "react";
import {Card, CardContent, CardHeader, Grid, Table, TableBody, TableCell, TableHead, TableRow} from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import Button from "@material-ui/core/Button";

class MonthFeature extends React.Component {

    now = new Date()
    state = {
        year: this.now.getFullYear(),
        month: this.now.getMonth() + 1,
        reload: true,
        data: [],
        types: []
    }

    componentDidMount() {
        this.loadData()
    }

    loadData() {
        const {year, month} = this.state;
        fetch(`/api/month?yearMonth=${year}-${String(month).padStart(2, "0")}`)
            .then(res => {
                return res.json()
            })
            .then(json => {
                const data = json.transactionSumByDestinationAndType
                const types = [...data
                    .map(it => it.type)
                    .reduce((acc, cur) => {
                        acc.add(cur);
                        return acc
                    }, new Set())]
                this.setState({data, types});
            })
    }

    handleDateClicked = (step) => () => {
        const month = this.state.month + step
        if (month === 0) {
            return this.setState({
                year: this.state.year - 1,
                month: 12
            }, this.loadData)
        }
        if (month === 13) {
            return this.setState({
                year: this.state.year + 1,
                month: 1
            }, this.loadData)
        }
        return this.setState({ month }, this.loadData)
    }

    renderHeader() {
        const {year, month} = this.state;
        return <Card>
            <CardContent>
                <Grid container>
                    <Grid item xs={2}>
                        <Typography variant="h5" gutterBottom>
                            {year} - {month}
                        </Typography>
                    </Grid>
                    <Grid item xs={10}>
                        <Button variant="contained" color="primary" onClick={this.handleDateClicked(-1)}>Back</Button>
                        <Button variant="contained" color="primary" onClick={this.handleDateClicked(+1)}>Next</Button>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    }

    renderTable(type) {
        const {data} = this.state;
        return <Grid container spacing={1}>
            <Grid item xs={12}>
                <Card key={type}>
                    <CardHeader title={type}/>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Destination</TableCell>
                                <TableCell>Total</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {data.filter(it => it.type === type).map(it =>
                                <TableRow key={type + it.destination}>
                                    <TableCell>{it.destination}</TableCell>
                                    <TableCell width={200}>€ {it.total}</TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </Card>
            </Grid>
        </Grid>
    }

    render() {
        const {types} = this.state;

        return <React.Fragment>
            <Grid container spacing={1}>
                <Grid item xs={12}>
                    {this.renderHeader()}
                </Grid>
                <Grid item xs={12}>
                    {types.length > 0
                        ? types.map(type => this.renderTable(type))
                        : <Card>
                            <CardContent>
                                <Typography variant="h5">No results</Typography>
                            </CardContent>
                        </Card>}
                </Grid>
            </Grid>
        </React.Fragment>


    }
}

export default MonthFeature;