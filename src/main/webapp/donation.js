jQuery(document).ready(function () {

  var signalStart = false

  // Parse URL
  var scriptURL = jQuery('script[src$="/donation.js"]').attr('src');
  if (scriptURL) {
    var parser = document.createElement('a');
    parser.href = scriptURL;
    var postUrl = parser.protocol + '//' + parser.host
  }

  // CONFIG
  var bankOptions = [
    {label: 'ABN AMRO', value: 'ABNANL2A'},
    {label: 'ASN Bank', value: 'ASNBNL21'},
    {label: 'ING BANK', value: 'INGBNL2A'},
    {label: 'Rabobank', value: 'RABONL2U'},
    {label: 'SNS Bank', value: 'SNSBNL2A'},
    {label: 'SNS Regio Bank', value: 'RBRBNL21'},
    {label: 'Triodos Bank', value: 'TRIONL2U'},
    {label: 'Van Lanschot', value: 'FVLBNL22'},
    {label: 'Knab', value: 'KNABNL2H'},
    {label: 'Bunq', value: 'BUNQNL2A'},
    {label: 'Moneyou', value: 'MOYONL21'}];

  var creditCardOptions = [
    {label: 'MasterCard', value: 'mastercard'},
    {label: 'Visa', value: 'visa'},
    {label: 'American Express', value: 'Amex'}];

  var hideClass = 'd-none';

  // ELEMENTS
  var anonymousCheckbox = document.querySelector('#anonymous');
  var amountRadioButtons = document.querySelector('#amount-radio');
  var idealSelectElement = document.querySelector('#ideal-select');
  var creditcardSelectElement = document.querySelector("#credit_card-select");
  var errorNotificationElement = document.querySelector('#error-notification');
  var paymentMethodRadioButtons = document.querySelector('#payment-method');
  var personalDataForm = document.querySelector('#personal-data');

  var sepaName = document.querySelector('#sepa-name');
  var sepaIban = document.querySelector('#sepa-iban');
  var sepaBic = document.querySelector('#sepa-bic');
  var sepaFrequency = document.querySelector('#sepa-frequency');
  var sepaCountry = document.querySelector('#sepa-country');

  // FUNCTIONS
  var getOptionElement = function (item) {
    var optionElement = document.createElement("option");
    optionElement.text = item.label;
    optionElement.value = item.value;
    return optionElement;
  };

  var addRequiredOnInput = function (el) {
    jQuery.each(el.querySelectorAll('input'), function () {
      if (this.hasAttribute("notrequired"))
        this.setAttribute("required", "");
      this.removeAttribute("notrequired");
    });
  };

  var removeRequiredOnInput = function (el) {
    jQuery.each(el.querySelectorAll('input'), function () {
      if (this.hasAttribute("required")) {
        this.setAttribute("notrequired", "");
        this.removeAttribute("required");
      }
    });
  };

  // SETTING UP FORM
  if (idealSelectElement) {
    jQuery.each(bankOptions, function () {
      idealSelectElement.appendChild(getOptionElement(this));
    });
  }

  if (creditcardSelectElement) {
    jQuery.each(creditCardOptions, function () {
      creditcardSelectElement.appendChild(getOptionElement(this));
    });
  }

  // VISIBILITY TOGGLES
  amountRadioButtons.addEventListener('click', function (ev) {
    var otherAmountInput = document.querySelector('#other-amount-input');
    var otherAmountInputField = document.querySelector('#other-amount-input-field');
    if (ev.target.value === 'other') {
      jQuery(otherAmountInput).show();
      jQuery(otherAmountInputField).prop('require', true);
    } else {
      jQuery(otherAmountInput).hide();
      jQuery(otherAmountInputField).prop('require', false);
    }
  });

  if (paymentMethodRadioButtons) {
    paymentMethodRadioButtons.addEventListener("click", function () {

      var idealCheckbox = document.querySelector('#ideal');
      var creditcardCheckbox = document.querySelector('#creditcard');
      var sepaCheckbox = document.querySelector('#sepa');

      var bankSelectInput = document.querySelector('#bank-select-input');
      var creditCardSelectInput = document.querySelector('#creditcard-select-input');
      var sepaSelectInput = document.querySelector('#sepa-select-input');

      if (idealCheckbox && idealCheckbox.checked) {
        jQuery(bankSelectInput).show();
        jQuery(creditCardSelectInput).hide();
        jQuery(sepaSelectInput).hide();

        jQuery(sepaName).prop('required', false);
        jQuery(sepaIban).prop('required', false);
        jQuery(sepaBic).prop('required', false);
      } else if (creditcardCheckbox && creditcardCheckbox.checked) {
        jQuery(creditCardSelectInput).show();
        jQuery(bankSelectInput).hide();
        jQuery(sepaSelectInput).hide();

        jQuery(sepaName).prop('required', false);
        jQuery(sepaIban).prop('required', false);
        jQuery(sepaBic).prop('required', false);
      } else if (sepaCheckbox && sepaCheckbox.checked) {
        jQuery(sepaSelectInput).show();
        jQuery(creditCardSelectInput).hide();
        jQuery(bankSelectInput).hide();

        jQuery(sepaName).prop('required', true);
        jQuery(sepaIban).prop('required', true);
        jQuery(sepaBic).prop('required', true);
        jQuery(sepaFrequency).prop('required', true);
      }
    });
  }

  if (anonymousCheckbox) {
    anonymousCheckbox.addEventListener("change", function () {
      if (anonymousCheckbox.checked && !personalDataForm.classList.contains(hideClass)) {
        personalDataForm.className += (" " + hideClass);
        removeRequiredOnInput(personalDataForm);
        toggleNewsLetter(false);
      } else if (!anonymousCheckbox.checked && personalDataForm.classList.contains(hideClass)) {
        personalDataForm.classList.remove(hideClass);
        addRequiredOnInput(personalDataForm);
        toggleNewsLetter(true)
      }
    });
  }

  // FORM SUBMIT
  document.addEventListener("submit", function (event) {
    event.preventDefault();
    donate();
  });

  var toggleErrorNotification = function (showError) {
    showError ? errorNotificationElement.classList.remove(hideClass) : errorNotificationElement.className += (" " + hideClass);
  };

  var toggleSpinner = function (showSpinner) {
    var submitButton = document.querySelector('button[type="submit"]');
    if (showSpinner) {
      submitButton.className += (" " + 'spinner');
      submitButton.disabled = true;
    } else {
      submitButton.classList.remove('spinner');
      submitButton.disabled = false;
    }
  };
  var toggleNewsLetter = function (enabledNewsletter) {
    var newsletterCheckbox = document.querySelector('#newsletter');
    if (enabledNewsletter) {
      newsletterCheckbox.disabled = false;
    } else {
      newsletterCheckbox.checked = false;
      newsletterCheckbox.disabled = true;
    }
  };

  function donate() {

    toggleErrorNotification(false);
    toggleSpinner(true);

    var donationData;
    var personalDataObj = {};

    var anonymous = anonymousCheckbox && anonymousCheckbox.checked;
    var agreedOnTerms = document.querySelector('#terms').checked;
    var newsletter = document.querySelector('#newsletter').checked;

    // Get payment data
    var paymentType = document.querySelector('input[name=paymentType]:checked');
    var issuer = paymentType && document.querySelector("#" + paymentType.value.toLowerCase() + "-select");
    var amountSelectValue = document.querySelector('input[name=amount]:checked');
    var amountInput = document.querySelector('#other-amount-input input');

    var groupInput = document.querySelector('#group');

    var amount;
    if (amountSelectValue.value === 'other') {
      amount = amountInput.value;
    } else {
      amount = amountSelectValue.value;
    }

    var bankAccount = sepaName && sepaIban && sepaBic && sepaCountry && {
      name: sepaName.value,
      iban: sepaIban.value,
      bic: sepaBic.value,
      country: sepaCountry.value
    }

    var donationData = {
      payment: {
        paymentType: paymentType ? paymentType.value : "SEPA",
        issuer: issuer && issuer.value,
        amount: amount,
        bankAccount: bankAccount,
        frequency: sepaFrequency && sepaFrequency.value,
      },
      newsletter: newsletter,
      agreedOnTerms: agreedOnTerms,
      group: groupInput && groupInput.value
    };
    if (!anonymous) {
      // Get personal data
      jQuery.each(personalDataForm.querySelectorAll('input[type="text"]'), function () {
        if (this.value === "") return;
        personalDataObj[this.id] = this.value;
      });
      personalDataObj.gender = personalDataForm.querySelector('input:checked').value;
      donationData.member = personalDataObj
    }
    var xhrObj = {
      'type': 'POST',
      'url': postUrl + '/api/donations/donate',
      'contentType': 'application/json',
      'data': JSON.stringify(donationData),
      'dataType': 'text',
      'success': function (response, status) {
        if (response) {
          window.location.href = response;
        }
      },
      'error': function (error) {
        toggleErrorNotification(true);
        toggleSpinner(false);
      }
    };
    return jQuery.ajax(xhrObj);
  }


  jQuery('#donation').change(function () {
    if (!signalStart) {
      signalStart = true
      console.log('Start server', signalStart)
      var xhrObj = {
        'type': 'GET',
        'url': postUrl + '/api/donations/donate'
      }
      return jQuery.ajax(xhrObj);
    }
  });


});