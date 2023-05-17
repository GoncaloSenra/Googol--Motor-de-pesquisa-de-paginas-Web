let stompClient = null;

function setConnected(connected) {

    $("#conversation").show();

    $("#messages").html("");
}

function connect() {
    const socket = new SockJS('/my-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, (frame) => {
        setConnected(true);
        console.log('Connected:', frame);
        stompClient.subscribe('/topic/messages', (message) => {
            showMessage(JSON.parse(message.body).content);
        });
    });
}

function sendMessage() {
    stompClient.send("/app/message", {}, JSON.stringify({'content': $("#message").val()}));
}

function showMessage(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', (e) => {
        e.preventDefault();
    });
    // Exercise 3.
    $("#connect").click(() => {
        connect();
    });
    $("#disconnect").click(() => {
        disconnect();
    });
    $("#send").click(() => {
        sendMessage();
    });
});

// Exercise 3.
/*
window.onload = connect;
window.onbeforeunload = disconnect;
*/
