import 'whatwg-fetch';
import 'babel-polyfill';
import React from "react";
import ReactDOM from "react-dom";

import DonationForm from "./form/DonationForm";

ReactDOM.render(<DonationForm/>, document.getElementById("root"));
