/**
*
*  WebToolKit.base64 (Base64 encode / decode) component
*  Compiled by Justas Vinevicius <justas.vinevicius(at)gmail.com>
*  Original code by Tyler Akins <fidian(at)rumkin.com>
*
*  Dependencies:
*  WebToolKit.utf8 (UTF-8 encode / decode) component for correct UTF-8 handling
*
*  Homepage:
*  http://www.webtoolkit.info/
*
**/

if (typeof(WebToolKit) == "undefined") {
	var WebToolKit = {};
};

WebToolKit.base64 = {

	keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

	encode : function (input) {
		var output = "";
		var chr1, chr2, chr3;
		var enc1, enc2, enc3, enc4;
		var i = 0;

		if (typeof(String.prototype.utf8encode) !== "undefined") {
			input = input.utf8encode();
		}

		do {
			chr1 = input.charCodeAt(i++);
			chr2 = input.charCodeAt(i++);
			chr3 = input.charCodeAt(i++);

			enc1 = chr1 >> 2;
			enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
			enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
			enc4 = chr3 & 63;

			if (isNaN(chr2)) {
				enc3 = enc4 = 64;
			} else if (isNaN(chr3)) {
				enc4 = 64;
			}

			output = output + this.keyStr.charAt(enc1) + this.keyStr.charAt(enc2) +
			this.keyStr.charAt(enc3) + this.keyStr.charAt(enc4);
		} while (i < input.length);

		return output;
	},

	decode : function (input) {
		var output = "";
		var chr1, chr2, chr3;
		var enc1, enc2, enc3, enc4;
		var i = 0;

		input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

		do {
			enc1 = this.keyStr.indexOf(input.charAt(i++));
			enc2 = this.keyStr.indexOf(input.charAt(i++));
			enc3 = this.keyStr.indexOf(input.charAt(i++));
			enc4 = this.keyStr.indexOf(input.charAt(i++));

			chr1 = (enc1 << 2) | (enc2 >> 4);
			chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
			chr3 = ((enc3 & 3) << 6) | enc4;

			output = output + String.fromCharCode(chr1);

			if (enc3 != 64) {
				output = output + String.fromCharCode(chr2);
			}
			if (enc4 != 64) {
				output = output + String.fromCharCode(chr3);
			}
		} while (i < input.length);

		if (typeof(String.prototype.utf8decode) !== "undefined") {
			return output.utf8decode();
		} else {
			return output;
		}
	}

};

if (typeof(String.prototype.base64encode) == "undefined") {
	String.prototype.base64encode = function () {
		return WebToolKit.base64.encode(this);
	};
};

if (typeof(String.prototype.base64decode) == "undefined") {
	String.prototype.base64decode = function () {
		return WebToolKit.base64.decode(this);
	};
};

