const formatter = new Intl.NumberFormat('nl-NL', {
  style: 'currency',
  currency: 'EUR',
  minimumFractionDigits: 2
});
const url = 'https://europe-west1-bring-the-elephant-home.cloudfunctions.net';
const path = '/donation-destination-balance?destination={{destination}}';
var oReq = new XMLHttpRequest();
oReq.addEventListener("load", function(){
  const data = JSON.parse(this.responseText)
  const element = document.getElementById("donation_destination_balance");
  element.innerHTML = formatter.format(data.amount)
});
oReq.open("GET", url + path);
oReq.send();
