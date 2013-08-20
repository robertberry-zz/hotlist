(jQuery(function ($) {
    var historyDiv = $("#history_hover");

    $(".show_tweet_history").mouseenter(function (event) {
        var elem = $(event.target);
        var historyPath = "/history/" + elem.attr("href");

        $.get(historyPath, function (data) {
            historyDiv.html(data);
            historyDiv.show();
        })
    });

    $(".show_tweet_history").mouseleave(function (event) {
        historyDiv.hide();
    });
}));