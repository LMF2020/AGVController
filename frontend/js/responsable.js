var evt = "onorientationchange" in window ? "orientationchange" : "resize";
window.addEventListener(evt, resize, false);

function resize(fals) {
	if(window.orientation == 0 || window.orientation == 180) {
		location.reload();
	} else {
		location.reload();
	}
}