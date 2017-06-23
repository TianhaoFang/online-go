let canvas = document.getElementById("app");
let context = canvas.getContext("2d");

const rowLength = 20;
const padLength = 18;
const rows = 15;

function rowToPosition(rowNum) {
    return padLength + rowNum * 20;
}

canvas.onmousedown = function () {
    const cW = context.canvas.width;
    context.beginPath();
    context.strokeStyle = "black";
    for(let i = 0; i < rows; i++){
        context.lineWidth = 2;

        context.moveTo(rowToPosition(0), rowToPosition(i));
        context.lineTo(rowToPosition(14), rowToPosition(i));

        context.moveTo(rowToPosition(i), rowToPosition(0));
        context.lineTo(rowToPosition(i), rowToPosition(14));

        context.stroke();
    }
    context.closePath();

    context.fillStyle = "black";
    context.moveTo(rowToPosition(10), rowToPosition(4));
    context.beginPath();
    context.arc(rowToPosition(10), rowToPosition(4), padLength / 2, 0, Math.PI * 2);
    context.closePath();
    context.fill();
    context.lineWidth = 0;
    context.strokeStyle = "red";
    context.stroke();

    context.fillStyle = "white";
    context.moveTo(rowToPosition(10), rowToPosition(5));
    context.beginPath();
    context.arc(rowToPosition(10), rowToPosition(5), padLength / 2, 0, Math.PI * 2);
    context.closePath();
    context.fill();
    context.lineWidth = 0;
    context.strokeStyle = "black";
    context.stroke();

    context.fillStyle = "white";
    context.moveTo(rowToPosition(9), rowToPosition(4));
    context.beginPath();
    context.arc(rowToPosition(9), rowToPosition(4), padLength / 2, 0, Math.PI * 2);
    context.closePath();
    context.fill();
    context.lineWidth = 0;
    context.strokeStyle = "black";
    context.stroke();

    context.fillStyle = "black";
    context.moveTo(rowToPosition(0), rowToPosition(0));
    context.beginPath();
    context.arc(rowToPosition(0), rowToPosition(0), padLength / 2, 0, Math.PI * 2);
    context.closePath();
    context.fill();
    context.lineWidth = 0;
    context.strokeStyle = "black";
    context.stroke();

    context.fillStyle = "white";
    context.moveTo(rowToPosition(0), rowToPosition(14));
    context.beginPath();
    context.arc(rowToPosition(0), rowToPosition(14), padLength / 2, 0, Math.PI * 2);
    context.closePath();
    context.fill();
    context.lineWidth = 0;
    context.strokeStyle = "black";
    context.stroke();
};