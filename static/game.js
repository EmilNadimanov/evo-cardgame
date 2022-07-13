var game = {}; // Namespace

(function () {
	"use strict";

	// Constants
	game.ENTRY_ID = 'entry';
	game.OUTPUT_ID = 'output';

	// Global variables
	game.user = null;
	game.type = null;
	game.ws = null;

	game.initPage = function () {
		console.log("initPage")
		document.getElementById(game.ENTRY_ID).focus();
		if (game.user == null)
			game.writeOutput('Initializing card game client.');
		game.writeOutput(game.user == null ? 'Enter your username:' : 'Enter desired game:');
	};

    game.connect = function() {
		console.log("initPage. game.user: " + game.user + ",game.type: " + game.type)

		var url = new URL('simpleCardGame/connect/' + encodeURI(game.type) + "/" + encodeURI(game.user), window.location.href);
        url.protocol = url.protocol.replace('http', 'ws');

        game.ws = new WebSocket(url.href);
        game.ws.onopen = function(evt) {
            game.writeOutput('Connection established');
            // game.writeOutput('Type \'/help\' for a list of commands');
        };

        game.ws.onclose = function(evt) {
            game.writeOutput('Disconnected from server');
        };

        game.ws.onmessage = function(evt) {
            // KeepAlive messages have no content
            if (evt.data !== '') {
                game.writeOutput(evt.data);
            }
            else {
                console.debug('KeepAlive received');
            }
        };

        game.ws.onerror = function(evt) {
            game.writeOutput('There was a communications error, check the console for details');
            console.error("WebSocket Error", evt)
        }
    };

	game.onEntryKeyPress = function (oCtl, oEvent) {
		if (game.isEnterKeyPress(oEvent)) {

			// Capture the current text as a command
			var sEntry = oCtl.value.trim();
			console.log("onEntryKeyPress. Entry:" + sEntry)

			// Reset the text entry for the next command
			oCtl.value = '';

			if (game.user === null) {
				// Set the username first if we still need one
				if (sEntry.length > 0) {
					game.user = sEntry;
					game.initPage()
				}
			}
			else if (game.type === null || game.ws == null) {
				// Set the username first if we still need one
				if (sEntry.length > 0) {
					game.type = sEntry;
					game.connect();
				}
			}
			else {
				// Process the entry
				if (sEntry !== '') {
    				game.ws.send(sEntry);
                }
			}
		}
	};

	game.isEnterKeyPress = function (oEvent) {
		var keynum;

		if (window.event) { // IE8 and earlier
			keynum = oEvent.keyCode;
		} else if (oEvent.which) { // IE9/Firefox/Chrome/Opera/Safari
			keynum = oEvent.which;
		}

		// Detect ENTER key
		return ('\n' === String.fromCharCode(keynum) || '\r' === String.fromCharCode(keynum));
	};

	game.writeOutput = function (sOutput) {
		var oOutput, sPadding;
		oOutput = document.getElementById(game.OUTPUT_ID);

		// Get a spacer unless we are the first entry
		sPadding = '\n';
		if (oOutput.value.length === 0) {
			sPadding = '';
		}

		// Append the output to the text area
		oOutput.value += sPadding + sOutput;

		// Scroll the text into view
		oOutput.scrollTop = oOutput.scrollHeight;
	};
}());
