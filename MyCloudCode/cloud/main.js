
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
var twilio = require("twilio");
twilio.initialize("ACfa702654457ff0df1dcf3c80b8aada7b","9f743bffa20b85af537adc46c0f417c6");
 
// Create the Cloud Function
Parse.Cloud.define("inviteWithTwilio", function(request, response) {
  // Use the Twilio Cloud Module to send an SMS
  twilio.sendSMS({
    From: "+13399874656",
    To: request.params.toNumber,
    Body: request.params.fromName + " (" + request.params.fromNumber + ") is trying to reach you via ListenApp! Download the app to reach them."
  }, {
    success: function(httpResponse) { response.success("Successfully sent the invitation"); },
    error: function(httpResponse) { response.error("Uh oh, something went wrong"); }
  });
});