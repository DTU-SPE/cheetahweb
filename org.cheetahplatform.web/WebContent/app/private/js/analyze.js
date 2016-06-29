var cheetah = cheetah || {};
var href = window.location.href;
var start = href.indexOf('=') + 1;
if (start === 0) {
    //no url parameter given --> redirect to search
    window.location.href = "search.htm";
}
//parsed the url parameters, see: http://stackoverflow.com/questions/2090551/parse-query-string-in-javascript
function parseUrl(url) {
    var query = {};
    var parameters = url.substr(1).split('&');
    for (var i = 0; i < parameters.length; i++) {
        var parameter = parameters[i].split('=');
        query[decodeURIComponent(parameter[0])] = decodeURIComponent(parameter[1] || '');
    }

    return query;
}

$(document).ready(function () {
    if (start === 0) {
        return;
    }

    var queryParameters = parseUrl(window.location.search);
    if (queryParameters.processInstance) {
        cheetah.processInstance = queryParameters.processInstance;
    }
    if (queryParameters.files) {
        cheetah.files = queryParameters.files.split(',').map(function (idAsString) {
            return Number(idAsString);
        });
    }
    if (queryParameters.movie) {
        cheetah.movie = Number(queryParameters.movie);
    }

    //initialize grid
    var margin = 3;
    var baseWidth = $('.gridster ul').width() / 6;
    var baseHeight = ($('.gridster ul').height() - 20) / 6;
    $(".gridster ul").gridster({
        widget_margins: [margin, margin],
        widget_base_dimensions: [baseWidth - margin, baseHeight - margin],
        autogrow_cols: true,
        resize: {
            enabled: true,
            stop: updateWidgets
        }
    });

    function updateWidgets() {
        cheetah.broadcast("cheetah-size");
    }

    cheetah.listeners = {};
    cheetah.on = function (id, listener) {
        var existing = cheetah.listeners[id];
        if (existing === undefined) {
            existing = [];
            cheetah.listeners[id] = existing;
        }

        existing.push(listener);
    };

    cheetah.broadcast = function (id, data) {
        var listeners = cheetah.listeners[id];
        if (listeners === undefined) {
            return;
        }

        $.each(listeners, function (index, listener) {
            listener(data);
        });
    };

    //bootstrap only those apps for which we have input; adapt the layout accordingly
    if (cheetah.processInstance || cheetah.files) {
        angular.bootstrap($('[ng-app="cheetah.Pupillometry"]'), ['cheetah.Pupillometry']);
        angular.bootstrap($('[ng-app="cheetah.Percentile"]'), ['cheetah.Percentile']);
    } else {
        $('#cheetah-analyze-pupillometry-container').remove();
        $('#cheetah-analyze-video-container').attr('data-sizex', 6).attr('data-col', 1);
    }

    if (cheetah.processInstance || cheetah.movie) {
        angular.bootstrap($('[ng-app="cheetah.Video"]'), ['cheetah.Video']);
    } else {
        $('#cheetah-analyze-video-container').remove();
        $('#cheetah-analyze-pupillometry-container').attr('data-sizex', 6);
    }

    if (cheetah.processInstance) {
        angular.bootstrap($('[ng-app="cheetah.Interactions"]'), ['cheetah.Interactions']);
    } else {
        $('#cheetah-analyze-interactions-container').remove();
        $('#cheetah-analyze-video-container').attr('data-sizey', 6);
    }
});