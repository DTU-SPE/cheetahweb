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

    angular.bootstrap($('[ng-app="cheetah.PupillometryWorkflow"]'), ['cheetah.PupillometryWorkflow']);
});