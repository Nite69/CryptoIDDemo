
	var xmlHttp = null;
	var forwardPage = 'error';
	var trigger = 'error';
	function pollRefresh() {
		var Url = "SessionLongPollWait?trigger="+trigger;
		//alert("Starting Long poll:"+Url);
		xmlHttp = new XMLHttpRequest(); 
		xmlHttp.onreadystatechange = ProcessRequest;
		xmlHttp.open("GET", Url, true );
		xmlHttp.send( null );
	}
	function ProcessRequest() {
		if ( xmlHttp.readyState == 4 ) {
			if (xmlHttp.status == 200 ) {
				alert(xmlHttp.responseText);
				window.location.replace(forwardPage);
			} else {
				//alert("Longpoll timeout");
				setTimeout(pollRefresh,300); 
			}
		}
	}
