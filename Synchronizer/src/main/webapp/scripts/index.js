$(document).ready(function () {
    $(document).on("click", "#submitBtn", function (event) {
        event.preventDefault();
        $("#submitBtn").hide();
        $.post("/performAction", {
            id: $("#id").val(),
            fileName: $("#fileName").val()
        }, function (data, status) {
            $("#submitBtn").show();
            alert(data);
        });
    });
});
