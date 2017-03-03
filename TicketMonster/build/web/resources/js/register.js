  $(document).ready(function() {
    $('#contact_form').bootstrapValidator({
        // To use feedback icons, ensure that you use Bootstrap v3.1.0 or later
        feedbackIcons: {
            valid: 'glyphicon glyphicon-ok',
            invalid: 'glyphicon glyphicon-remove',
            validating: 'glyphicon glyphicon-refresh'
        },
        fields: {
			username: {
				validators: {
					stringLength: {
					min: 4,
					}
				},
					notEmpty: {
					message: 'Please supply your username'
					}
			},
            email: {
                validators: {
                    notEmpty: {
                        message: 'Please supply your email address'
                    },
                    emailAddress: {
                        message: 'Please supply a valid email address'
                    }
                }
            },
			password: {
				validators: {
					stringLength: {
					min: 8,
					},
					identical: {
                    field: 'password_confirm',
                    message: 'The password and its confirm are not the same'
                }
				},
				notEmpty: {
				message: 'Please supply a longer password'
				}
			},
			password_confirm: {
				validators: {
					stringLength: {
					min: 8,
					},
					identical: {
                    field: 'password',
                    message: 'The password and its confirm are not the same'
                }
				},
				notEmpty: {
				message: 'Please supply a longer password'
				}
			}
        }
       
    })

});
